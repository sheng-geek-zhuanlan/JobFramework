
package com.sheng.jobframework.utility.HttpClient;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 *  Utility class for base64 encoding and decoding.
 */

public class Base64 {
    /*
     *  Default alphabet for base64-encoded data.
     */

    private static final char[] alphabet =
    { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
      'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b',
      'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
      'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3',
      '4', '5', '6', '7', '8', '9', '+', '/' };

    /*
     *  Inverse of the  i-to-alphabet[i] mapping.
     */

    private static final Map<Character, Short> reverseAlphabet =
        new HashMap<Character, Short>();

    static {
        for (short i = 0; i < 64; i++) {
            reverseAlphabet.put(alphabet[i], i);
        }
    }

    /**
     *  Standard base64 encoder.
     *  Equivalent to calling to64(bs,null,null,null);
     *
     *  @param bs Byte data to be encoded.
     *  @return Base64 encoding of bs.
     */

    public static String to64(byte[] bs) throws Base64Exception {
        return to64(bs, null, null, null);
    }

    /**
     *  Customizable base64 encoder.
     *
     *  This encoder allows the caller to customize the
     *  encoded output in the following ways:
     *
     *  <ul>
     *  <li>
     *
     *      The characters used to represent the base 64
     *      values "62" and "63" can be changed from the
     *      standard characters, "+" and "/" respectively.
     *      If the "extra" parameter is null, the standard
     *      characters are used.  If the "extra" parameter
     *      is an array of length 1, extra[0]
     *      is used to encode "63" instead of "/", and
     *      "62" is still encoded as "+".  If the "extra"
     *      parameter is an array of length 2 or more,
     *      extra[0] is used to encode "62"
     *      instead of "+", and extra[1] is used
     *      to encode "63" instead of "/".  If either of
     *      extra[0] or extra[1] is a Roman
     *      letter or a digit, a Base64Exception is thrown.
     *
     *  </li>
     *  <li>
     *
     *      The character used to pad the base64 encoding
     *      when the input is not a multiple of 3 bytes can
     *      be changed from the standard padding character,
     *      "=".  If the "padchar" parameter is null,
     *      the "=" is used as the padding character.
     *      Otherwise, the value of "padchar" is used
     *      as the padding character.  If the output
     *      is padded, and padchar is a Roman letter,
     *      a digit, or the encoding of "62" or "63",
     *      a Base64Exception is thrown.
     *
     *  </li>
     *  <li>
     *
     *      The base64 output can be unpadded.  If the
     *      "padding" parameter is null, or is true,
     *      the output is padded when the input is not a
     *      multiple of 3 bytes.  Otherwise, the output
     *      is unpadded.
     *
     *  </li>
     *  </ul>
     *
     *  @param bs Byte data to be encoded.
     *  @param extra Last character, or last two characters, to be used for the base64 values "63", or "62" and "63", instead of "+" and "/".
     *  @param padchar Character to be used for padding instead of the standard "=".
     *  @param padding Whether to pad the output if the input is not a multiple of 3 bytes.
     *  @return Base64 encoding of bs.
     */

    public static String to64(byte[] bs, char[] extra, Character padchar,
                              Boolean padding) throws Base64Exception {
        // Encoding of null is null.

        if (bs == null) {
            return null;
        }

        // Number of user-specified extra characters

        int numExtra = 0;

        if (extra != null) {
            numExtra = extra.length;

            if (numExtra > 2) {
                numExtra = 2;
            }

            for (int i = 0; i < numExtra; i++) {
                for (int j = 0; j < 64 - numExtra; j++) {
                    if (extra[i] == alphabet[j]) {
                        throw new Base64Exception("Extra bytes overlap standard alphabet");
                    }
                }
            }
        }

        if (padchar == null) {
            padchar = '=';
        }

        if (padding == null) {
            padding = true;
        }

        if (padding) {
            for (int i = 0; i < numExtra; i++) {
                if (extra[i] == padchar) {
                    throw new Base64Exception("Pad character is an extra byte");
                }
            }

            for (int i = 0; i < 64 - numExtra; i++) {
                if (alphabet[i] == padchar) {
                    throw new Base64Exception("Pad character is in standard alphabet");
                }
            }
        }

        StringBuffer b64 = new StringBuffer();

        // Buffer to hold triplets of input bytes as short integers.

        short[] buff = new short[3];

        /*
         *  Number of pad characters if the triplet is
         *  not all input bytes
	 */

        int pad = 0;

        // Loop through input data 3 bytes at a time

        for (int offset = 0; offset < bs.length; offset += 3) {
            // Index into current triplet of input bytes

            int i = 0;

            for (; (i < 3) && (offset + i < bs.length); i++) {
                // Byte is co-erced to short integer

                buff[i] = bs[offset + i];

                // Make short integer unsigned if the byte
                // was negative

                if (buff[i] < 0) {
                    buff[i] += 256;
                }
            }

            // Pad input with 0's and compute number of pad integers

            pad = 0;

            for (; i < 3; i++) {
                buff[i] = 0;
                pad++;
            }

            // Encoding value from 0-63

            int base64value;

            // First base 63 is top 6 bits of first byte

            base64value = buff[0] / 4;

            if (base64value < 64 - numExtra) {
                b64.append(alphabet[base64value]);
            } else {
                b64.append(extra[base64value - 64 + numExtra]);
            }

            /*
             *  Second base 64 is rightmost 2 digits of
             *  the first byte, followed by leftmost 4
             *  digits of the second byte.
	     */

            base64value = (16 * (buff[0] % 4)) + (buff[1] / 16);

            if (base64value < 64 - numExtra) {
                b64.append(alphabet[base64value]);
            } else {
                b64.append(extra[base64value - 64 + numExtra]);
            }

            /*
             *  If 0 or 1 pad bytes were added, third base
             *  64 is rightmost 4 digits of the secnd
             *  byte, followed by leftmost 2 digits of
             *  the third byte.
	     */

            if (pad < 2) {
                base64value = (4 * (buff[1] % 16)) + (buff[2] / 64);

                if (base64value < 64 - numExtra) {
                    b64.append(alphabet[base64value]);
                } else {
                    b64.append(extra[base64value - 64 + numExtra]);
                }
            }

            /*
             *  If no pad bytes were added, the fourth
             *  base 64 is the rightmost 6 bits of the
             *  third byte.
	     */

            if (pad < 1) {
                base64value = buff[2] % 64;

                if (base64value < 64 - numExtra) {
                    b64.append(alphabet[base64value]);
                } else {
                    b64.append(extra[base64value - 64 + numExtra]);
                }
            }
        }

        // Add pad characters, if padding.

        if (padding) {
            for (; pad > 0; pad--) {
                b64.append(padchar);
            }
        }

        return b64.toString();
    }

    /**
     *  Standard base64 decoder.
     *
     *  Equivalent to from64(b64,null,null,null).
     *
     *  @param b64 Base64-encoded string to be decoded.
     *  @return Array of decoded bytes.
     */

    public static byte[] from64(String b64) throws Base64Exception {
        return from64(b64, null, null, null);
    }

    /**
     *  Customizable base64 decoder.
     *
     *  <p>
     *
     *  This decoder allows the user to customize
     *  the decoding of a base64 string.  The "extra",
     *  "padchar", and "padded" parameters have the same
     *  meanings as in the 4-parameter version of "to64"
     *
     *  </p>
     *  <p>
     *
     *  A Base64Exception will also be thrown by this
     *  method if it encounters a base64 digit that is
     *  not in the specified alphabet.
     *
     *  </p>
     *  <p>
     *
     *  <b>Note:</b> if a sequence of bytes has been
     *  encoded with a certain customization, it should
     *  be decoded with the same customization, or the
     *  data may be incorrectly decoded.
     *
     *  </p>
     */

    public static byte[] from64(String b64, char[] extra, Character padchar,
                                Boolean padded) throws Base64Exception {
        // Decoding of null is null.

        if (b64 == null) {
            return null;
        }

        // Number of user-specified extra characters.

        int numExtra = 0;

        if (extra != null) {
            numExtra = extra.length;

            if (numExtra > 2) {
                numExtra = 2;
            }

            for (int i = 0; i < numExtra; i++) {
                for (int j = 0; j < 64 - numExtra; j++) {
                    if (extra[i] == alphabet[j]) {
                        throw new Base64Exception("Extra bytes overlap standard alphabet");
                    }
                }
            }
        }

        if (padchar == null) {
            padchar = '=';
        }

        if (padded == null) {
            padded = true;
        }

        if (padded) {
            for (int i = 0; i < numExtra; i++) {
                if (extra[i] == padchar) {
                    throw new Base64Exception("Pad character is an extra byte");
                }
            }

            for (int i = 0; i < 64 - numExtra; i++) {
                if (alphabet[i] == padchar) {
                    throw new Base64Exception("Pad character is in standard alphabet");
                }
            }
        }

        // Decoded bytes

        List<Byte> bs = new LinkedList<Byte>();

        // Buffer of quadruplets of base64 digits

        short[] buff = new short[4];

        // Loop through inputs, 4 base64 characters as a time

        for (int offset = 0; offset < b64.length(); offset += 4) {
            // Number of actual input, non-pad characters
            // in the quadruplet

            int numBase64 = 0;

            for (;
                 (numBase64 < 4) && (offset + numBase64 < b64.length()) && (!padded ||
                                                                            (b64.charAt(offset +
                                                                                        numBase64) !=
                                                                             padchar));
                 numBase64++) {
                // Current base 64 digit

                char base64digit = b64.charAt(offset + numBase64);

                // Base 64 (0-63) value of a base64 digit

                Short base64value = null;

                /*
                 *  See if digit is a user-specified
                 *  extra digit
		 */

                for (short i = 0; i < numExtra; i++) {
                    if (base64digit == extra[i]) {
                        base64value = i;
                        break;
                    }
                }

                if (base64value == null) {
                    // Not a user-specific extra digit

                    base64value = reverseAlphabet.get(base64digit);

                    if ((base64value != null) &&
                        (base64value >= 64 - numExtra)) {
                        /*
                         *  Don't use standard extra
                         *  characters overridden by user
			 */

                        base64value = null;
                    }
                }

                if (base64value == null) {
                    throw new Base64Exception("Character \"" + base64digit +
                                              "\" is not in the base 64 alphabet");
                }

                buff[numBase64] = base64value;

            }

            // Decoded byte as an int

            int byteAsInt;

            /*
             *  First decoded byte is the first base64
             *  followed by the leftmost 2 bits of the
             *  second base64.
	     */

            byteAsInt = (4 * buff[0]) + (buff[1] / 16);

            if (byteAsInt > 127) {
                byteAsInt -= 256;
            }

            bs.add((byte)byteAsInt);

            /*
             *  If at least 3 non-pad base64s, second
             *  decoded byte is the rightmost 4 bits of
             *  the second base64 followed by the leftmost
             *  4 bits of the third base64.
	     */

            if (numBase64 > 2) {
                byteAsInt = (16 * (buff[1] % 16)) + (buff[2] / 4);

                if (byteAsInt > 127) {
                    byteAsInt -= 256;
                }

                bs.add((byte)byteAsInt);
            }

            /*
             *  If no pad digits, third decoded byte is
             *  the rightmost 2 bits of th third base64,
             *  followed by the bits of the fourth base64.
	     */

            if (numBase64 > 3) {
                byteAsInt = (64 * (buff[2] % 4)) + buff[3];

                if (byteAsInt > 127) {
                    byteAsInt -= 256;
                }

                bs.add((byte)byteAsInt);
            }
        }

        byte[] bsa = new byte[bs.size()];
        int i = 0;

        for (Byte b : bs) {
            bsa[i] = b;
            i++;
        }

        return bsa;
    }

}
