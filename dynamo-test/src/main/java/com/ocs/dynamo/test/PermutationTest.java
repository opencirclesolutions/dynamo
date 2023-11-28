package com.ocs.dynamo.test;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class PermutationTest {

    @Test
    void test() {
        permutate(new int[]{ 1,2,3,4}, 0);
    }

    @Test
    void test2() {
        var plan = 1;
        plan = plan++ + --plan;
        System.out.println(plan);
    }

    public void permutate(int[] p, int first) {
        if (first == p.length-1) {
            System.out.println(Arrays.toString(p));
        }
        for (int i = first; i < p.length; i++) {
            swap(p,first,i);
            permutate(p, first+1);
            swap(p,i,first);
        }
    }

    private void swap(int[] p, int i, int j) {
        int x = p[i];
        p[i] = p[j];
        p[j] = x;
    }


}
