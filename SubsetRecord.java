/*
/*
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.*;

/**
 *
 * @author yanan
 */
public class SubsetRecord {
    public int number;                //number of basic terms in the subset
    public float bestcost;           //
    public SubsetRecord left;
    public SubsetRecord right;
    public int noBranching;         //
    public float selectivity;    //product p of the selectivities of all terms
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
