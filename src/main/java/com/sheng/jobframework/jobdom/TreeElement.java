package com.sheng.jobframework.jobdom;

import com.sheng.jobframework.utility.Utility;

import com.sheng.jobframework.observer.ObserverSubscriber;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Observer;
import java.util.Properties;


public class TreeElement extends ACElement {

    public ArrayList elementNode = new ArrayList(10000);
    public Properties elementProp = new Properties();
    public TreeElement parentJob = null;
    public String parentName = "";

    private int parentId;
    private int selfId;
    protected String nodeName;

    public TreeElement() {
        elementNode = new ArrayList(1000);
        elementProp = new Properties();
    }

    public static void setObserver(ObserverSubscriber observerList) {
        observerSubscriber = observerList;
    }

    public void setParent(TreeElement job) {
        parentJob = job;
    }

    public void addChildNode(TreeElement subJob) {
        subJob.setParent(this);
        elementNode.add(subJob);
    }

    public TreeElement getParent() {
        return parentJob;
    }

    public void removeChildNode(TreeElement subJob) {
        elementNode.remove(subJob);
        subJob.parentJob = null;
    }

    public void replaceChildNode(TreeElement tobereplaced,
                                 TreeElement replaced) {
        int i = elementNode.indexOf(tobereplaced);
        elementNode.set(i, replaced);
        replaced.setParent(this);
    }

    public void emptyAllChildNodes() {
        int size = getChildNodesNum();
        elementNode.clear();
    }

    public boolean isLeaf() {
        if (elementNode == null) {
            return true;
        } else {
            return elementNode.isEmpty();
        }
    }

    public int getChildNodesNum() {
        return elementNode.size();
    }

    public int getBrotherNodesNum() {
        return parentJob.getChildNodesNum();
    }

    public TreeElement getChildNodesByIndex(int i) {
        return (TestJobElement)elementNode.get(i);
    }

    public TreeElement getBrotherNodesByIndex(int i) {
        return (TestJobElement)parentJob.getChildNodesByIndex(i);
    }

    public TreeElement getChildNodeByName(String childName) {
        int iChilds = getChildNodesNum();
        for (int i = 0; i < iChilds; i++) {
            TreeElement child = getChildNodesByIndex(i);
            if (child.getName().equalsIgnoreCase(childName)) {
                return child;
            }
        }
        return null;
    }

    public ArrayList getAllChildNodeByName(String childName) {
        ArrayList nodeArr = new ArrayList(50);
        int iChilds = getChildNodesNum();
        for (int i = 0; i < iChilds; i++) {
            TreeElement child = getChildNodesByIndex(i);
            if (child.getName().equalsIgnoreCase(childName)) {
                nodeArr.add(child);
            }
            if (child.getChildNodesNum() > 0) {
                ArrayList childArr = child.getAllChildNodeByName(childName);
                Utility.mergeTwoArrayList(nodeArr, childArr);
            }
        }
        return nodeArr;
    }
    //all B's child node will be under A's, B is a lonley obsolete node

    public void mergeNode2Me(TreeElement eleB) {
        TreeElement parentB = eleB.getParent();
        int iChilds = eleB.getChildNodesNum();
        for (int i = 0; i < iChilds; i++) {
            TreeElement childEle = eleB.getChildNodesByIndex(i);
            //      eleB.removeChildNode(childEle);
            addChildNode(childEle);
        }
        if (parentB != null) {
            parentB.removeChildNode(eleB);
        } else {
            outputLog("!!!!ele B is a lone job " + eleB.getName());
        }
    }

    public TreeElement getPreviousBrotherJob() {
        if (parentJob.getChildNodesNum() > 1) {
            return parentJob.getChildNodesByIndex(parentJob.getChildNodesNum() -
                                                  2);
        } else {
            //outputFrameLog("ERROR in get PreviousBrotherJob: job<"+getName()+"> is the first child, has no previous brother node");
            return null;
        }
    }

    public TreeElement getCurrentChildJob() {
        if (ifHasChildNodes()) {
            return getChildNodesByIndex(getChildNodesNum() - 1);
        } else {
            outputFrameLog("ERROR in get getCurrentChildJob: job<" +
                           getName() + "> has no child Job!");
            return null;
        }
    }

    public TreeElement getBrotherNodeByName(String brotherName) {
        int iChilds = parentJob.getChildNodesNum();
        for (int i = 0; i < iChilds; i++) {
            TreeElement child = parentJob.getChildNodesByIndex(i);
            if (child.getName().equalsIgnoreCase(brotherName)) {
                return child;
            }
        }
        return null;
    }

    public boolean ifHasChildNodes() {
        return !elementNode.isEmpty();
    }

    public void outputString() {
        elementProp.list(System.out);
        for (int i = 0; i < getChildNodesNum(); i++) {
            TreeElement ele = getChildNodesByIndex(i);
            ele.getElementProperty().list(System.out);
            //System.out.println(ele.getElementProperty().toString());
        }
    }

    public String getProperty(String key) {
        Enumeration elmentInfoKeys = elementProp.keys();
        while (elmentInfoKeys.hasMoreElements()) {
            String strKey = (String)elmentInfoKeys.nextElement();
            if (strKey.equalsIgnoreCase(key))
                return elementProp.getProperty(strKey);
        }
        return "";
    }

    public Properties getElementProperty() {
        return elementProp;
    }

    public void setName(String value) {
        elementProp.put("name", value);
    }

    public void addObserver(Observer observer) {
        observerSubscriber.addObserver(observer);
    }

    public String getName() {
        return getProperty("name");
    }

    public int getIndexByNodeName(TreeElement ele) {
        return elementNode.indexOf(ele);
    }

    public void setChildNode(int pos, TreeElement ele) {
        elementNode.set(pos, ele);
    }

    public boolean insertParent(TreeElement ele) {


        TreeElement parentEle = getParent();
        if (parentEle == null) {
            outputFrameLog("ERROR when restructure testJob: ele " +
                           ele.getName() + " has already been root element!");
            return false;
        }
        int index = parentEle.getIndexByNodeName(this);
        parentEle.setChildNode(index, ele);
        ele.setParent(parentEle);
        ele.addChildNode(this);
        return true;

    }

    public void deleteParent() {

        //TO DO: there is still bug when insert node in the tree
        TreeElement parentEle = getParent();
        TreeElement gratherFatherEle = parentEle.getParent();
        gratherFatherEle.removeChildNode(parentEle);
        parentEle.setParent(null);
        gratherFatherEle.addChildNode(this);
    }

    public void deleteChild(TreeElement ele) {
        removeChildNode(ele);
        ele.setParent(null);
        int isize = ele.getChildNodesNum();
        for (int i = 0; i < isize; i++) {
            TreeElement child = ele.getChildNodesByIndex(i);
            addChildNode(child);
        }
    }

    public ArrayList getAllLeaveNodes(TreeElement ele) {
        ArrayList eleArr = new ArrayList(100);
        if (!ele.ifHasChildNodes()) {
            //ele.outputString();
            eleArr.add(ele);
            return eleArr;
        }
        int isize = ele.getChildNodesNum();
        for (int i = 0; i < isize; i++) {
            TreeElement childEle = ele.getChildNodesByIndex(i);
            ArrayList childEleArr = getAllLeaveNodes(childEle);
            eleArr = Utility.mergeTwoArrayList(eleArr, childEleArr);
        }
        return eleArr;

    }

    public void insertChild(int index, TreeElement ele) {
        ele.setParent(this);
        elementNode.add(index, ele);
    }

    public void insertFirstChild(TreeElement ele) {
        ele.setParent(this);
        elementNode.add(0, ele);
    }
}
