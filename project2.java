/*
 * COMS4112 Project2
 * main function to run the optimization algorithm and output the results
 */


import java.util.*;
import java.io.*;
/**
 *
 * @author yanan, Li-Chieh Liu 
 */

public class project2 {

    /**
     * @param args the command line arguments
     */
    
    /*
        In this code, we initially want to use stdout for outputing, 
        but we finally use a finalstr which keep appending the small segment of result, and print the finalstr all together at the same time.
        So you might see a lot of commented System.out.println(), just forget about it. We apology.
    */
    
    
    // The below function is used to append the small segment of result to out finalstr.
    public static String printResults(ArrayList<SubsetRecord> basicplans, ArrayList<Float> products) {
        String finalstr = "";
        finalstr += "==================================================================\n";

        // System.out.println("==================================================================");
        String s = "";
        for (Float p : products) {
            s += String.valueOf(p) + " ";
        }
        // System.out.println(s);
        // System.out.println("------------------------------------------------------------------");
        finalstr += s;
        finalstr += "\n------------------------------------------------------------------\n";

        SubsetRecord optimal = basicplans.get(basicplans.size()-1);
        ArrayList<SubsetRecord> logicTerms = new ArrayList<>();
        helper.getLogicAndTerms(optimal, logicTerms);

        if (optimal.left == null && optimal.right == null && optimal.noBranching == 1) {
            // System.out.println("answer[j] = i;");
            // System.out.print("j += ");         
            // System.out.print(getLogicTermStr(optimal));
            // System.out.println(";");
            finalstr += "answer[j] = i;\n";
            finalstr += "j += ";
            finalstr += getLogicTermStr(optimal);
            finalstr += ";\n";

        } else {
            String out = printOptimalPlan(logicTerms);
            finalstr += out;
        }
        // System.out.println("------------------------------------------------------------------");
        // System.out.print("cost:");
        // System.out.println(optimal.bestcost);

        finalstr += "------------------------------------------------------------------\n";
        finalstr += "cost:";
        finalstr += optimal.bestcost;
        finalstr +="\n";
        return finalstr;
    }
    
    // This function is to write our output to "output.txt" 
    public static void writeToFile(String finalstr) {
        try {
            File file = new File("output.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file);
            fw.write(finalstr);
            fw.close();
        } catch (IOException e) {
             e.printStackTrace();
        }
    }
     

    // concatenate an '&' term to string, since it don't have left right children.
    public static String getLogicTermStr(SubsetRecord sub) {
        ArrayList<Integer> ele = new ArrayList<>(sub.elements);
        String str = "";

        for (int i = 0; i < ele.size(); i++) {
            str += "t" + ele.get(i) + "[o" + ele.get(i) + "[i]"+ "]";
            if (i != ele.size()-1) {
                str+= " & ";
            }           
        }
        // str += ")";
        if (sub.elements.size() == 1) {
            return str;
        } else {
            return "(" + str + ")";
        }
        
    }
  /* 
    This function is to see if the SubsetRecord Object have left and right children, if it don't have any child, 
    it means that the SubsetRecord Object is just an &-term, no sub &-term inside of it, so it will be concatenated by an '&' (done in getLogicTermStr()).
    Otherwise, the left and right children exist as a pair, so that we know it should be concatenated with an '&&'.
  */
  public static String printOptimalPlan(ArrayList<SubsetRecord> subsets) {
        String finalout = "";
        String noBranch ="";
        finalout += "if (";
        // System.out.print("if (");
        for (int i=0; i < subsets.size(); i++) {
            if (subsets.get(i).noBranching == 0) {
                String out ="";
                if (subsets.get(i).elements.size() != 1) {
                    out += getLogicTermStr(subsets.get(i));
                } else {
                    out += getLogicTermStr(subsets.get(i));
                }
           
                // System.out.print(out);
                finalout += out;
            }
           
            if (i != subsets.size()-1) {
                if (subsets.get(i+1).noBranching == 0) {
                    // System.out.print(" && ");
                    finalout += " && ";
                } else {
                    noBranch = getLogicTermStr(subsets.get(i+1));
                }
            }
            
        }
        // System.out.println(" {") ;
        finalout += ") {\n";
        if (noBranch.length() == 0) {
            // System.out.println("\tanswer[j++] = i;\n}") ;
            finalout += "    answer[j++] = i;\n}";
        } else {
            String out = "    answer[j] = i;\n    j += " + noBranch +";";
            out += "\n}";
            // System.out.println(out);
            finalout += out +"\n";
        }
        return finalout;
    }

    // this is the core implementation of Algorithms 4.11
    public static String optimize(ArrayList<Float> products) {
        //generate all 2^k-1 basic plans with all &-terms
        ArrayList<SubsetRecord> basicplans = helper.generateBasicPlans(products);
        //iterate in the basicplans and update the optimal sub-plan and cost
        for (SubsetRecord s: basicplans) {
            for (SubsetRecord s2 : basicplans) {        
                HashSet<Integer> retain = new HashSet<Integer>(s2.elements);
                retain.retainAll(s.elements);
                if (retain.size()!= 0) {
                    continue;
                } 
                
                if (helper.dominateCmetrics(s2, s)) {
                    //do nothing by lemma 4.8
                    continue;
                } else if (s2.selectivity <=0.5 && helper.dominateDmetrics(s2, s)){
                    //do nothing by lemma 4.9
                    continue;
                } else {
                    //calculate the cost for (s'&&s)    
                    float p = s2.selectivity;
                    float q = Math.min(p, 1 - p);
                    float unionCost = helper.getFcost(s2) + helper.getMval()*q + p*s.bestcost;
                    //update Uninton SubsetRecord
                    int index = helper.findUnionIndex(s, s2, basicplans);
                    SubsetRecord unionSub = basicplans.get(index);
                    if (unionCost < unionSub.bestcost) {
                        unionSub.bestcost = unionCost;
                        unionSub.left = s2;
                        unionSub.right = s;  
                    } 
                }             
            }
        }
        return printResults(basicplans, products);    
    }
    public static void main(String[] args) {
        // initialize selectivites and the properties
        ArrayList<ArrayList<Float>> products = new ArrayList<>();
        Properties props = new Properties();

        //load the query and config files 
        if (args.length != 2) {
            System.out.println("Please enter with query file name and config file name");
            return;
        }
        String configFile = args[1];
        String queryFile  = args[0];
        helper.loadProps(props, configFile);

        //parse query file to get all the productivity
        File query = new File(queryFile);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(query));
            String sr;
            while ((sr = reader.readLine()) != null) {
                // System.out.println(sr); 
                String[] strs = sr.split(" ");
                // System.out.println(sr); 
                ArrayList<Float> p = new ArrayList<Float>();
                for (String s : strs) {
                    p.add(Float.parseFloat(s));
                }
                products.add(p);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String res="";
        for (ArrayList<Float> selectivities : products) {
            res += optimize(selectivities);
        }
        
        //print the results to consol
        System.out.print(res);
        //write the results to output.txt
        writeToFile(res);
        return;    
    }
    
}
