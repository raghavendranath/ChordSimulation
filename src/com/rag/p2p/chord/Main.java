package com.rag.p2p.chord;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;


import org.apache.commons.io.FileUtils;

public class Main {
    //These are for clients and hash Functions values
   static HashMap<String,BigInteger> dhMap = new HashMap<>();
   static TreeMap<BigInteger, String> sMap = new TreeMap<>();

    //These are for books and hash Functions values
    static HashMap<String,BigInteger> dhMapBooks = new HashMap<>();
    static TreeMap<BigInteger, String> sMapBooks = new TreeMap<>();

    //These are for fingerTable lookups
    static ArrayList<Client> clients = new ArrayList<>();
    static ArrayList<String> clientIndex = new ArrayList<>();

    static int mod_number = -1;

    public static void main(String[] args) throws NoSuchAlgorithmException {
        // For clients

        Scanner sc = new Scanner(System.in);
        System.out.println("To how many bits you want the chord system to truncate(m):");
        int m = Integer.parseInt(sc.nextLine());
        mod_number = (int) Math.pow(2,m);
        System.out.println("Enter number of clients:");
        int n = Integer.parseInt(sc.nextLine());

        boolean success = (new File( "C:\\Users\\ragha\\Desktop\\Chord")).mkdirs();
        if(!success){
            System.out.println("Problem with client java file");
            System.exit(0);
        }
        MessageDigest md = MessageDigest.getInstance("MD5");
        for(int i=0; i<n;i++){
            Client new_peer = new Client("Client"+i,m);
            if(!clientIndex.contains(new_peer)){
                clients.add(new_peer);
                clientIndex.add(new_peer.ID);
            }
            success = (new File( "C:\\Users\\ragha\\Desktop\\Chord\\Client"+i )).mkdirs();
            if(!success){
                System.out.println("Folder not created");
                System.exit(0);
            }
            md.update(new_peer.ID.getBytes(), 0, new_peer.ID.length());
            BigInteger id = new BigInteger(1, md.digest()).mod(new BigInteger(Integer.toString(mod_number)));
            System.out.println(new_peer.ID+":"+id);
            if(!dhMap.containsKey(new_peer.ID)){
                dhMap.put(new_peer.ID, id);
            }
            if(!sMap.containsKey(id)){
                sMap.put(id, new_peer.ID);
            }
        }
        Random random = new Random();

        for(int i=0; i<n;i++){
            //for adding books into the network
            ArrayList<String> bookIndexes = new ArrayList<>();
            for(int j=0; j< 5;j++){
                String temp = "book"+i+""+random.nextInt(10);
                if(!bookIndexes.contains(temp)){
                    bookIndexes.add(temp);
                    try{
                        md.update(temp.getBytes(), 0, temp.length());
                        //System.out.println(Integer.toString(mod_number));
                        BigInteger bookID = new BigInteger(1, md.digest()).mod(new BigInteger(Integer.toString(mod_number)));
                        String peerID = placeForFile(sMap, bookID);
                        if(peerID != null){
                            System.out.println("BookName: "+temp+"is getting stored in "+peerID+". And it's hash value is "+bookID);
                            int index = clientIndex.indexOf(peerID);
                            Client presentClient = clients.get(index);
                            if(!presentClient.books.contains(temp)){
                                presentClient.books.add(temp);
                                PrintWriter writer = new PrintWriter("C:\\Users\\ragha\\Desktop\\Chord\\"+peerID+"\\"+temp+".txt");
                                writer.append("This books is with client "+i+". And the book name is "+temp);
                                writer.close();

                                //For keeping track of books
                                if(!dhMapBooks.containsKey(temp)){
                                    dhMapBooks.put(temp, bookID);
                                }
                                if(!sMapBooks.containsKey(bookID)){
                                    sMapBooks.put(bookID, temp);
                                }
                            }
                          /* PrintWriter writer = new PrintWriter("C:\\Users\\ragha\\Desktop\\Chord\\"+peerID+"\\"+temp+".txt");
                            writer.append("This books is with client "+i+". And the book name is "+temp);
                            writer.close();

                            //For keeping track of books
                            if(!dhMapBooks.containsKey(temp)){
                                dhMapBooks.put(temp, bookID);
                            }
                            if(!sMapBooks.containsKey(bookID)){
                                sMapBooks.put(bookID, temp);
                            }*/
                        }
                    }
                    catch(Exception ie){
                        ie.printStackTrace();
                    }
                }

            }


        }


        //For initializing the finger tables
        for(Client client:clients){
            client.fingers_Initialize();
            //client.printFingerTable();
            client.prec = find_Predecssor(client);
        }

/*
        //for updating the predecessor for each node
        Main.find_Predecssor();
*/


        //for print each node
        for(Client client: clients){
            System.out.println("This is "+client.ID);
            System.out.println("+++++++++++++++++++++++++++++++++");
            System.out.println("Predecessor::"+client.prec.ID);
            System.out.println("Successor:"+client.succ.ID);
            client.printFingerTable();
        }


        boolean loopFlag = true;
        Stabilize stProtocol = new Stabilize();
        stProtocol.flag = true;
        Thread t = new Thread(stProtocol);
        t.start();

        while(loopFlag){
            System.out.println("Select an option:");
            System.out.println("1. Work with a node.");
            System.out.println("2. Node join");
            System.out.println("3. Node Leaving");
            System.out.println("4. Exit");
            String ch = sc.nextLine().trim();
            switch(ch){
                case "1":
                    boolean loopFlag1 = true;
                    while(loopFlag1){
                        System.out.println("Below are the options you have:");
                        System.out.println("1. Choose a Node");
                        System.out.println("2. exit");
                        String selection1 = sc.nextLine().trim();
                        switch(selection1){
                            case "1":
                                boolean loopFlag2 = true;
                                System.out.println("Choose a node:");
                                String acting_peer = sc.nextLine();
                                int index = clientIndex.indexOf(acting_peer);
                                if(index == -1){
                                    System.out.println("Sorry the node is not present");
                                    break;
                                }
                                Client peer = clients.get(index);
                                if(!new File("C:\\Users\\ragha\\Desktop\\Chord\\"+peer.ID).exists()){
                                    removePeer(peer);
                                    loopFlag1 = false;
                                    break;
                                }
                              while(loopFlag2){
                                    System.out.println("1. Search for a book");
                                    System.out.println("2. Delete a node");
                                    System.out.println("3. print finger table entries");
                                    System.out.println("4. List out the books present in this node");
                                    System.out.println("5. exit");
                                    String selection2 = sc.nextLine().trim();
                                    switch(selection2){
                                        case "1":
                                            System.out.println("Enter the book Name");
                                            String searchBook = sc.nextLine();
                                            if(peer.books.contains(searchBook)){
                                                System.out.println("Book is present with you");
                                                break;
                                            }
                                            System.out.println("Searching for the book");
                                            Client foundPeer = findBook(peer, searchBook);
                                            if(foundPeer == null){
                                                System.out.println("Sorry the book is not present in the network");
                                                break;
                                            }
                                            System.out.println("Found book at "+foundPeer.ID);
                                            System.out.println("Downloading the file from the "+foundPeer.ID);
                                            File source = new File("C:\\Users\\ragha\\Desktop\\Chord\\"+foundPeer.ID+"\\"+searchBook+".txt");
                                            if(!source.exists()){
                                                System.out.println("Sorry the file is not avaiable with the peer because peer is down!");
                                                removePeer(foundPeer);
/*                                                for(String book: foundPeer.books){
                                                    BigInteger value = dhMapBooks.get(book);
                                                    sMapBooks.remove(value);
                                                    dhMapBooks.remove(book);
                                                }
                                                int tempIndex = clientIndex.indexOf(foundPeer.ID);
                                                clientIndex.remove(tempIndex);
                                                clients.remove(tempIndex);
                                                BigInteger value = dhMap.get(foundPeer.ID);
                                                sMap.remove(value);
                                                dhMap.remove(foundPeer.ID);*/
                                                break;
                                            }
                                            File dest = new File("C:\\Users\\ragha\\Desktop\\Chord\\"+peer.ID);
                                            try {
                                                FileUtils.copyFileToDirectory(source, dest);
                                                peer.books.add(searchBook);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            System.out.println("Book is downloaded. Have a good day!");
                                            break;
                                        case "2":
                                            if(new File("C:\\Users\\ragha\\Desktop\\Chord\\"+peer.ID).exists()) {
                                                try {
                                                    FileUtils.deleteDirectory(new File("C:\\Users\\ragha\\Desktop\\Chord\\" + peer.ID));
                                                } catch (IOException ie) {
                                                    ie.printStackTrace();
                                                }
                                            }

                                            removePeer(peer);
                                            /*for(String book: peer.books){
                                                BigInteger value = dhMapBooks.get(book);
                                                sMapBooks.remove(value);
                                                dhMapBooks.remove(book);
                                            }
                                            int tempIndex = clientIndex.indexOf(peer.ID);
                                            clientIndex.remove(tempIndex);
                                            clients.remove(tempIndex);
                                            BigInteger value = dhMap.get(peer.ID);
                                            sMap.remove(value);
                                            dhMap.remove(peer.ID);*/

                                            loopFlag2 = false;
                                             break;
                                        case "3":
                                            peer.printFingerTable();
                                            break;
                                        case "4":
                                            peer.printBooks();
                                            break;
                                        case "5":
                                            loopFlag2 = false;
                                            break;

                                      default:
                                            System.out.println("Invalid selection");
                                    }

                                }


                                break;
                            case "2":
                                loopFlag1 = false;
                                break;
                            default:
                                System.out.println("Invalid selection");

                        }

                    }
                    break;
                case "2":
                    System.out.println("Enter the client ID:");
                    String new_peer = sc.nextLine();
                    if(clientIndex.contains(new_peer)){
                        System.out.println("This client is already present. Please select another name. Going back.");
                        break;
                    }
                    Client peer = new Client(new_peer,m );
                    System.out.println("Enter the node you know:");
                    String known_node = sc.nextLine();
                    if(!clientIndex.contains(known_node)){
                        System.out.println("Sorry the node is not present");
                        break;
                    }
                    clientIndex.add(new_peer);
                    clients.add(peer);
                    success = (new File( "C:\\Users\\ragha\\Desktop\\Chord\\"+peer.ID )).mkdirs();
                    if(!success){
                        System.out.println("Folder not created");
                        System.exit(0);
                    }
                    md.update(peer.ID.getBytes(), 0, peer.ID.length());
                    BigInteger id = new BigInteger(1, md.digest()).mod(new BigInteger(Integer.toString(mod_number)));
                    System.out.println(peer.ID+":"+id);
                    if(!dhMap.containsKey(peer.ID)){
                        dhMap.put(peer.ID, id);
                    }
                    if(!sMap.containsKey(id)){
                        sMap.put(id, peer.ID);
                    }
                    peer.fingers_Initialize();
                    peer.prec = find_Predecssor(peer);


                    System.out.println("Copying files to this peer");
                    int pred_ID = dhMap.get(peer.prec.ID).intValue();
                    int succ_ID = dhMap.get(peer.succ.ID).intValue();
                    int myID = id.intValue();
                    ArrayList<String> booksToBeStored = new ArrayList<>();
                    for (Map.Entry<BigInteger, String> entry : sMapBooks.entrySet()){
                        if(entry.getKey().intValue() > pred_ID && entry.getKey().intValue()<myID){
                            booksToBeStored.add(entry.getValue());
                            System.out.print(entry.getKey()+","+entry.getValue());
                        }
                    }

                    for(String book: booksToBeStored){
                        if(!peer.books.contains(book)){
                            peer.books.add(book);
                            try {
                                PrintWriter writer = new PrintWriter("C:\\Users\\ragha\\Desktop\\Chord\\" + peer.ID + "\\" + book + ".txt");
                                writer.append("This books is with " + peer.ID + "_. And the book name is " + book);
                                writer.close();
                                boolean result = Files.deleteIfExists(new File("C:\\Users\\ragha\\Desktop\\Chord\\"+peer.succ.ID+"\\"+book+".txt").toPath());
                                if(result){
                                    int index1=  -1;
                                    index1 = peer.succ.books.indexOf(book);
                                    if(index1!=-1)
                                         peer.succ.books.remove(index1);
                                }
                            }
                            catch(FileNotFoundException ie){
                                ie.printStackTrace();
                            }
                            catch(IOException ie){
                                ie.printStackTrace();
                            }

                        }

                    }
                    peer.prec.fingers_Initialize();
                    peer.prec.prec = find_Predecssor(peer.prec);

                    peer.succ.fingers_Initialize();
                    peer.succ.prec = find_Predecssor(peer.succ);

                    break;
                case "3":
                    break;
                case "4":
                    loopFlag = false;
                    //For stopping th stabilization protocol
                    stProtocol.flag = false;
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid selection");

            }




        }

        sc.close();


    }

    public static String placeForFile(TreeMap<BigInteger, String> sMap, BigInteger bookID){
        BigInteger present = null;
        for (Map.Entry<BigInteger, String> entry : sMap.entrySet()) {
           int res = bookID.compareTo(entry.getKey());
           if(res == -1){
               present = entry.getKey();
               break; //second value is greater than first
           }
        }
        if(present == null)//that means the book has to go to the first node
        {
            present = sMap.firstKey();
        }

        if(present!=null && sMap.containsKey(present))
            return sMap.get(present);
        else 
            return null;

    }


    public static Client findNodeInRange(int startId, int endId, String id){
        boolean firstFlag = false;
        int index = -1;
        String client = null;
        for (Map.Entry<BigInteger, String> entry : sMap.entrySet()) {
            int startResult = BigInteger.valueOf(startId).compareTo(entry.getKey());
            int endResult = BigInteger.valueOf(endId).compareTo(entry.getKey());
            if(id.equals(entry.getValue()))
                continue;
            if(startResult == 0){ //Correct
                client = entry.getValue();
                index =  clientIndex.indexOf(client);
                return clients.get(index);

            }
            if(startResult == -1 && endResult == -1){ //Both are less - correct
                client = entry.getValue();
                index =  clientIndex.indexOf(client);
                return clients.get(index);

            }

            if(startResult == -1 && endResult ==1){ //In between the range : First is less and second is great- correct
                client = entry.getValue();
                index =  clientIndex.indexOf(client);
                return clients.get(index);
            }

            if(startResult == 1 && endResult == -1 && !firstFlag) { //First is great and second is less- correct
                client = entry.getValue();
                index =  clientIndex.indexOf(client);
                firstFlag = true;
            }



/*
            if(startResult == 0 || endResult == 0){ //Correct
                String client = entry.getValue();
                int index =  clientIndex.indexOf(client);
                return clients.get(index);
            }

            if(startResult == -1 && endResult ==1){ //In between the range : First is less and second is great- correct
                String client = entry.getValue();
                int index =  clientIndex.indexOf(client);
                return clients.get(index);
            }

            if(startResult == -1 && endResult == -1){ //Both are less - correct
                String client = entry.getValue();
                int index =  clientIndex.indexOf(client);
                return clients.get(index);
            }

            if(startResult == 1 && endResult == -1) { //First is great and second is less- correct
                String client = entry.getValue();
                int index =  clientIndex.indexOf(client);
                return clients.get(index);

            }

            if(startResult == 1 && endResult == 1) { //if both are greater- correct
                String client = entry.getValue();
                int index =  clientIndex.indexOf(client);
                return clients.get(index);

            }*/

        }

        if(firstFlag){
                if(index!=-1)
                    return clients.get(index);
        }

        Map.Entry<BigInteger, String> entry =sMap.firstEntry();
        int startResult = BigInteger.valueOf(startId).compareTo(entry.getKey());
        int endResult = BigInteger.valueOf(endId).compareTo(entry.getKey());
        if(startResult == 1 && endResult == 1) //Both are more
        {
            if(!id.equals(entry.getValue())){
                client = entry.getValue();
                index =  clientIndex.indexOf(client);
                return clients.get(index);

            }

        }


        System.out.println("hello");
        return null;

    }


    public static Client findBook(Client peerID, String searchBook) throws NoSuchAlgorithmException{
        if(!dhMapBooks.containsKey(searchBook))
            return null;
        Client temp = peerID;
        BigInteger b = dhMapBooks.get(searchBook);
        boolean firstFlag = false;
        while(true){
            //Get the finger entry using client findRangeInFinger
            //int i = b.compareTo(dhMap.get(temp.ID));
            if(peerID == temp){
                if(!firstFlag){
                    firstFlag = true;
                }
                else{
                    break;
                }
            }
            int index = temp.findRangeInFinger(b.intValue());
            Client nextPeer = temp.fingers[index].node;
            if(nextPeer.books.contains(searchBook)){
                return nextPeer;
            }
            temp = nextPeer;

        }

        return null;
    }

 /*   //For finding the predecssor of a node
    public static void find_Predecssor(){
        for(Client client: clients){
            //boolean predFlag = true;
            Client temp = client;
            int i=-1;
            while(true) {
                if (temp.fingers[0].node != client) {
                    temp = temp.fingers[0].node;
                    i++;
                } else {
                    break;
                }
                if(i==clients.size()){
                    break;
                }
            }
           client.prec = temp;
        }
    }*/
    //For finding the predecssor of a node
    public static Client find_Predecssor(Client client) throws NoSuchAlgorithmException{
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(client.ID.getBytes(), 0, client.ID.length());
        BigInteger id = new BigInteger(1, md.digest()).mod(new BigInteger(Integer.toString(mod_number)));
        String prev = null;
        boolean flag = false;
        String temp = null;
        for(Map.Entry<BigInteger, String> me: sMap.entrySet()){
            if(id.intValue() > me.getKey().intValue()){
                prev = me.getValue();
                flag = true;
            }
            //TO get track of last element
            temp = me.getValue();
       }
       if(flag){
            int index =  -1;
            index = clientIndex.indexOf(prev);
            if(index != -1){
                return clients.get(index);
            }
       }
       else{
           //for the initial element
           int index =  -1;
           index = clientIndex.indexOf(temp);
           if(index != -1){
               return clients.get(index);
           }
       }
       return null;

    }

    //for removing details about the peer
    public static void removePeer(Client peer) throws NoSuchAlgorithmException {
        for(String book: peer.books){
            BigInteger value = dhMapBooks.get(book);
            sMapBooks.remove(value);
            dhMapBooks.remove(book);
        }
        int tempIndex = clientIndex.indexOf(peer.ID);
        clientIndex.remove(tempIndex);
        clients.remove(tempIndex);
        BigInteger value = dhMap.get(peer.ID);
        sMap.remove(value);
        dhMap.remove(peer.ID);


        //Updating the finger tables of successor and predeccessor
        if(peer.prec!=null){
            peer.prec.fingers_Initialize();
            peer.prec.prec = find_Predecssor(peer.prec);
        }
        if(peer.succ!=null){
            peer.succ.fingers_Initialize();
            peer.succ.prec = find_Predecssor(peer.succ);
        }
/*        //updating the predecessor
        find_Predecssor();*/
    }


    public static class Stabilize extends Thread{
        boolean flag = false;
        public void run()
        {
            while(flag){
                for(int i= 0; i< clients.size();i++){
                    Client client = clients.get(i);
                    try{
                        if(new File("C:\\Users\\ragha\\Desktop\\Chord\\"+client.ID).exists()){
                            client.fingers_Initialize();
                            client.prec = find_Predecssor(client);
                        }
                        else{
                            removePeer(client);
                        }

                        try{
                            //System.out.println("Accessing "+client.ID);
                            Thread.sleep(20000);
                        }
                        catch(InterruptedException ex){
                            //Logger.getLogger(com.rag.p2p.chord.Main.Stabilize.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }catch(NoSuchAlgorithmException ie){
                        ie.printStackTrace();
                    }
                }


            }

        }


    }
}
