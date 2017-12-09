package com.rag.p2p.chord;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Scanner;


public class Client {
    String ID;
    finger[] fingers;
    ArrayList<String> books;
    Client succ = null;
    Client prec = null;
    Client(String ID, int m){
        this.ID = ID;
        fingers = new finger[m];
        books = new ArrayList<>();
    }

    void fingers_Initialize() throws NoSuchAlgorithmException{
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(ID.getBytes(), 0, ID.length());
        BigInteger id = new BigInteger(1, md.digest()).mod(new BigInteger(Integer.toString((int)Math.pow(2,fingers.length))));
        int size = (int)Math.pow(2,fingers.length);
        fingers[0] = new finger();
        fingers[0].interval_start = (Integer.parseInt(id.toString())+(int)Math.pow(2,0))%size;
        fingers[0].interval_end = Integer.parseInt(id.toString())+(int)Math.pow(2,0)%size;
        Client node= Main.findNodeInRange(fingers[0].interval_start,fingers[0].interval_end, ID);
        if(node == null){
            System.out.print("There is a problem in your chord simulation");
            System.exit(0);
        }
        fingers[0].node = node;
        for(int i=1; i<fingers.length;i++){
            fingers[i] = new finger();
            fingers[i].interval_start = (fingers[i-1].interval_end+1)%size;
            fingers[i].interval_end = (fingers[i].interval_start+(int)Math.pow(2,i)-1)%size;
            node= Main.findNodeInRange(fingers[i].interval_start,fingers[i].interval_end, ID);

            if(node == null){
                System.out.println("i is:"+i);
                System.out.println("Start time and end time are:"+fingers[i].interval_start+","+fingers[i].interval_end);
                System.out.print("There is a problem in your chord simulation");
                System.exit(0);
            }
            fingers[i].node = node;
        }

        this.succ = fingers[0].node;

    }


    void printFingerTable(){
        System.out.println("Finger table of "+this.ID);
        System.out.println("+++++++++++++++++++++++++++++++++++");
        for(int i=0; i<this.fingers.length;i++){
            System.out.println(this.fingers[i].interval_start+"-"+this.fingers[i].interval_end+"  "+this.fingers[i].node.ID);
        }
        System.out.println();
        System.out.println();
    }

    int findRangeInFinger(int id){
        for(int i=0; i<fingers.length;i++){
            if(fingers[i].interval_start <= id){
                if(fingers[i].interval_end >= id){
                    return i;
                }
                if(fingers[i].interval_start  > fingers[i].interval_end){
                    if(fingers[i].interval_end < id){
                        return i;
                    }
                }
            }
        }
        return -1;
    }


    void printBooks(){
        for(String book: this.books){
            System.out.println(book);
        }
    }



}


class finger
{
    int interval_start;
    int interval_end;
    Client node;
}
