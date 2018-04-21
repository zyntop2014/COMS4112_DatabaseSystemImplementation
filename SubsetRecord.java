/*
 * COMS4112 Project2
 * SubsetRecorc class: a strcure that stores the information with the expressions (subset).
 */

import java.util.*;

/**
 *
 * @author yanan, Li-Chieh Liu 
 */
public class SubsetRecord {
    public int number;                //number of basic terms in the subset
    public float bestcost;           //
    public SubsetRecord left;
    public SubsetRecord right;
    public int noBranching;         //
    public float selectivity;    //product p of the selectivities of all terms
    
    /*
     For example, if the input query is 0.7, 0.6, 0.4.
     Since here, we use 1,2,3, etc. to represent those functions 0.7, 0.6, 0.4 repectively. 
     So, here, this hash set stores all functions who contribute to this subset. So if the subset is {0.6. 0.4}, 
      the elements in this hash set are 2, 3. 
    */ 
    public HashSet<Integer> elements; 
    
    
    public SubsetRecord(){
        this.left = null;
        this.right = null;
        this.bestcost = 0;
        this.noBranching = 0;
        this.selectivity = 0; 
        this.number = 0;
        this.elements = new HashSet<>();
    }
    
    public SubsetRecord(int number, float bestcost, SubsetRecord left, 
                        SubsetRecord right, int noBranching, int bitmask, 
                        float selectivity, HashSet<Integer> elements){
        this.number = number;
        this.bestcost = bestcost;
        this.left = left;
        this.right = right;
        this.noBranching = noBranching;
        this.selectivity = selectivity;
        this.elements = elements;
    }
    
 
    
}
