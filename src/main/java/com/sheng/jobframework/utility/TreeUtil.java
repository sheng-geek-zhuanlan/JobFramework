package com.sheng.jobframework.utility;

import com.sheng.jobframework.jobdom.ACElement;
import com.sheng.jobframework.jobdom.ACJavaJob;
import com.sheng.jobframework.jobdom.TestJobElement;
import com.sheng.jobframework.jobdom.TreeElement;
import com.sheng.jobframework.annotation.JobStatus;
import com.sheng.jobframework.annotation.TestJobType;
import com.sheng.jobframework.annotation.TestResult;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;


public class TreeUtil extends ACElement {
    public TreeUtil() {
        super();
    }

    public static Hashtable groupJob(TreeElement tree, String filter) {
        Hashtable hash = new Hashtable();
        int iTrans = tree.getChildNodesNum();
        for (int i = 0; i < iTrans; i++) {
            TestJobElement transJob =
                (TestJobElement)tree.getChildNodesByIndex(i);
            String transname = transJob.getName();
            if (transname.startsWith(ACJavaJob.TAG_PREFIX)) {
                if (hash.get(transname) == null) {
                    ArrayList pairJobArr = new ArrayList(20);
                    pairJobArr.add(transJob);
                    hash.put(transname, pairJobArr);
                } else {
                    ArrayList pariJobArr = (ArrayList)hash.get(transname);
                    pariJobArr.add(transJob);
                }
                // a lot of thing to be done here
                //transSuite.removeChildNode(transJob);
            }
        }
        return hash;
    }

    public static void sortByTime(ArrayList timeArr) {
        //ArrayList retArr = new ArrayList(20);
        //System.out.println("#####-start sroting");
        //System.out.println("timeArr size is "+timeArr.size());
        for (int i = 0; i < timeArr.size(); i++) {
            //for(int j=timeArr.size()-1;j>i;j--) {
            for (int j = 0; j < timeArr.size() - i - 1; j++) {
                TestJobElement jobA = (TestJobElement)timeArr.get(j);
                TestJobElement jobB = (TestJobElement)timeArr.get(j + 1);
                long timeA = jobA.getBeginTimeL();
                //System.out.println("i is "+i+" j is "+j);
                //System.out.println("first job begin time is "+timeA);
                long timeB = jobB.getBeginTimeL();
                //System.out.println("second job begin time is "+timeB);
                if (timeA > timeB) {
                    timeArr.set(j + 1, jobA);
                    timeArr.set(j, jobB);
                }
            }
        }
        //outputLog("#####-end sroting");
    }

    public static void caculateTransJob(Hashtable hash) {
        Enumeration names = hash.keys();
        while (names.hasMoreElements()) {
            String transname = (String)names.nextElement();
            //outputLog("#####-Caculate "+transname);
            ArrayList timeArr = (ArrayList)hash.get(transname);
            sortByTime(timeArr);
            int ileng = timeArr.size();
            for (int i = 1; i < ileng; i++) {
                TestJobElement baseTimeJob = (TestJobElement)timeArr.get(0);
                TestJobElement parentTimeJob = baseTimeJob.getParentJob();
                TestJobElement timeJob = (TestJobElement)timeArr.get(i);
                String name = timeJob.getName();
                TestJobElement distribute_trans =
                    new TestJobElement(TestJobType.TEST);

                distribute_trans.setName(ACJavaJob.PREFIX_DIS_T + name);
                distribute_trans.addProperty(ACJavaJob.PROP_START_DESC,
                                             baseTimeJob.getProperty(ACJavaJob.PROP_TS_DESC));
                distribute_trans.setBeginTime(baseTimeJob.getBeginTimeL());
                TestJobElement beginTimeJob =
                    JobUtil.deepCloneJob(baseTimeJob);
                distribute_trans.mergeNode2Me(beginTimeJob);

                TestJobElement step = new TestJobElement(TestJobType.STEP);
                step.addProperty(JobStatus.PASSED,
                                 "Distributed Start Transaction<" +
                                 distribute_trans.getName() +
                                 "> with description " +
                                 baseTimeJob.getProperty(ACJavaJob.PROP_TS_DESC));
                distribute_trans.addChildJob(step);

                parentTimeJob.addChildJob(distribute_trans);


                distribute_trans.addProperty(ACJavaJob.PROP_END_DESC,
                                             timeJob.getProperty(ACJavaJob.PROP_TS_DESC));
                TestJobElement step2 = new TestJobElement(TestJobType.STEP);
                TestJobElement endTimeJob = JobUtil.deepCloneJob(timeJob);
                distribute_trans.mergeNode2Me(endTimeJob);
                long endTimeL = timeJob.getEndTimeL();
                //step2.addProperty("passed", "Distributed Transaction<"+distribute_trans.getName()+"> end at time: "+endTimeL);
                long costtime = endTimeL - distribute_trans.getBeginTimeL();
                TestJobElement step3 = new TestJobElement(TestJobType.STEP);
                step3.addProperty(JobStatus.PASSED,
                                  "Distributed Transaction<" +
                                  distribute_trans.getName() +
                                  "> cost time: " + costtime);
                distribute_trans.addChildJob(step2);
                distribute_trans.addChildJob(step3);
                distribute_trans.setResults(TestResult.PASS);
                outputLog("******transaction <" + distribute_trans.getName() +
                          ">  end at time " + endTimeL);
                distribute_trans.setEndTime(timeJob.getEndTimeL());
            }

        }
    }

    public static void main(String[] args) {
        // Hashtable hash = new Hashtable();
        //System.out.println(hash.get("test"));
        TestJobElement jobA = new TestJobElement();
        jobA.setName("A");
        jobA.setBeginTime(1);
        TestJobElement jobB = new TestJobElement();
        jobB.setName("B");
        jobB.setBeginTime(5);
        TestJobElement jobC = new TestJobElement();
        jobC.setName("C");
        jobC.setBeginTime(2);
        ArrayList tempArr = new ArrayList(20);
        tempArr.add(jobA);
        tempArr.add(jobB);
        tempArr.add(jobC);
        TestJobElement job1 = (TestJobElement)tempArr.get(0);
        String name11 = job1.getName();
        job1 = (TestJobElement)tempArr.get(1);
        String name21 = job1.getName();
        job1 = (TestJobElement)tempArr.get(2);
        String name31 = job1.getName();
        System.out.println(name11 + ":" + name21 + ":" + name31);
        TreeUtil.sortByTime(tempArr);
        TestJobElement job = (TestJobElement)tempArr.get(0);
        String name1 = job.getName();
        job = (TestJobElement)tempArr.get(1);
        String name2 = job.getName();
        job = (TestJobElement)tempArr.get(2);
        String name3 = job.getName();
        System.out.println(name1 + ":" + name2 + ":" + name3);

    }
}
