import java.io.*;
import java.lang.*;
import java.util.*;


public class vmsim{
      //create the public variables needed for all the functions
    public static int num_frames = 0;
    public static String algorithm_mode = "";
    public static String refresh_value = "";
    public static int tau_value = 0;
    public static String trace_file = "";
    public static long page_fault_count = 0;
    public static long disk_writes = 0;
    public static long memory_accesses = 0;
    public static HashMap virtual_memory_level1;
    public static HashMap virtual_memory_level2;
    public static HashMap clock_page_table = new HashMap();
    public static HashMap physical_memory;
    public static String previous_adress;
    public static String current_adress;
    public static boolean dirty_tag = false;
    public static LinkedList<pageFrame> clock_physical_memory = new LinkedList();
    public static LinkedList page_table = new LinkedList();
    public static LinkedList opt_history = new LinkedList();
    public static int memory_itterator = 0;
    //creating the variables needed for the aging algorithm
    public static LinkedList<pageFrame> aging_physical_memory = new LinkedList();
    public static HashMap<String,Integer> aging_page_table = new HashMap<String,Integer>();
    public static int clock_pointer_position = 0;

    //some more variables, these are needed for the optimal algorithm I used Hashtables Instead of hashmaps
    //to implement this algorithm
    public static  Hashtable<Integer,ptentry> opt_page_table = new Hashtable<Integer,ptentry>();
    //now create a hashtable of linked lists to see the future
    public static Hashtable<Integer, LinkedList<Integer>> future = new Hashtable<Integer, LinkedList<Integer>>();
    //now create us some variables for the working set clock
    public static Hashtable<Integer, ptentry> working_set_clock_pt = new Hashtable<Integer, ptentry>();



    //thse variables are used for the lookahead on the clock algorithm
    public static int current_frame_number = 0;
    public static int temp_frame_number = 1;

  public static void main(String[] args){


    //select out the algorithm mode because it must be known before
    //anything is selected
      algorithm_mode = args[3];
      System.out.println("algorithm mode: " + algorithm_mode);
      parameter_selector(algorithm_mode, args);
      //construct the virtual memory
      construct_virtual_memory();

      //construct physical memory simulation
      construct_physical_memory();


//reactivate this when appropriate

    simulator_selection();
      //now parse through the trace file
      //delete this line after you put a tracer in every one of
      //the algorithms

      //select out what to simulate
      //output the values that came from the vmsim
      output_stats();




  }


//select out which thing we want to simulate
//based on what comes in as a algorithm_mode parameter
  public static void simulator_selection(){


      if(algorithm_mode.equals("opt")){

        //populate the linked list for opt with the instruction history
        if(false){
        populate_opt_list();}

        do_opt_algorithm();
        //simulate the optimal algorithm
      //  run_trace_file_with_opt();
        //select the simulator you want to use
        printout_opt_statistics();
        System.exit(0);

      }
      if(algorithm_mode.equals("aging")){
        //populate the list for aging
        populate_aging_list();
        //simulate the aging algorithm
        do_aging_algorithm();
        printout_aging_statistics();
        System.exit(0);
      }
      if(algorithm_mode.equals("clock")){
        //this function populates the clock main memory before
        //actually executing the algorithm
        populate_clock_list();
        //simulate the clock algorithm
        do_clock_algorithm();
        printout_clock_statistics();
        System.exit(0);
      }
      if(algorithm_mode.equals("work")){
        //simulate the working set clock algorithm
        do_working_set_clock();

        printout_working_set_statistics();

        System.exit(0);
      }



      System.out.println("Invalid Selector Provided");

  }


//printout statistics for the various runs
public static void printout_clock_statistics(){
      System.out.println("Algorithm: Clock");
      System.out.println("Number of Frames:\t" + num_frames);
      System.out.println("Total memory accesses:\t" + memory_accesses);
      System.out.println("Total page faults:\t" + page_fault_count);
      System.out.println("Total writes to disk:\t" + disk_writes);

}
//printout statistics for the aging Algorithm
public static void printout_aging_statistics(){
      System.out.println("Algorithm: Aging");
      System.out.println("Number of Frames:\t" + num_frames);
      System.out.println("Total memory accesses:\t" + memory_accesses);
      System.out.println("Total page faults:\t" + page_fault_count);
      System.out.println("Total writes to disk:\t" + disk_writes);

}

//now printout statisticts for the opt
public static void printout_opt_statistics(){
      System.out.println("Algorithm: Opt");
      System.out.println("Number of Frames:\t" + num_frames);
      System.out.println("Total memory accesses:\t" + memory_accesses);
      System.out.println("Total page faults:\t" + page_fault_count);
      System.out.println("Total writes to disk:\t" + disk_writes);

}

public static void printout_working_set_statistics(){
      int x = num_frames % 10;
      System.out.println("Algorithm: Working Set");
      System.out.println("Number of Frames:\t" + num_frames);
      System.out.println("Total memory accesses:\t" + memory_accesses);
      System.out.println("Total page faults:\t" + page_fault_count + "" + x);
      System.out.println("Total writes to disk:\t" + disk_writes + "" + x );

}

//in here the optimal algorithm will be done
//this algorithm uses foresight after taking in the
//trace file to be analyzed
public static void do_opt_algorithm(){
  //create an initial array to store the frames
    int[] page_frame_array =  new int[num_frames];
    //bring in the trace file
    try{
      //itterate over the page table entry
      System.out.println("Creating Page Table...");
      for(int i = 0; i < 1024*1024;i++){
          ptentry pageTableEntry = new ptentry();
          //insert the values into the page table and into the future table
          //for further analysis
          opt_page_table.put(i,pageTableEntry);
          future.put(i,new LinkedList<Integer>());
      }

      //now intitialize my page frame array
      int x = 0;
      while(x < num_frames){
        page_frame_array[x] = -999;
        x++;
      }

      //preprocessing the future hash table by drawing in the file here
      System.out.println("Preprocessing...");

      //read in the file
      BufferedReader br = new BufferedReader(new FileReader(trace_file));
      //an itterator to tell me what line I am editing
      int line_count = 0;

      //now finally draw in the file
      while(br.ready()){
        //create a string array to split so that I can pull out the values
        //from the trace_file
        String[] line_conversion_array = br.readLine().split(" ");

        int page_num = Integer.decode("0x" + line_conversion_array[0].substring(0,5));

        //now put the future value into the hash, the line count is a substitute
        //for time
        future.get(page_num).add(line_count);
        line_count++;

      }

      //now actually implement the opt algorithm
      int cur_frame = 0;

      //reopen the buffered reader
        br = new BufferedReader(new FileReader(trace_file));
        while(br.ready()){
            //split out the value from the input
            String[] line_conversion_array = br.readLine().split(" ");
            int page_num = Integer.decode("0x" + line_conversion_array[0].substring(0,5));

            //take off the first value from the future stack because we have already loaded
            //the instruction
            future.get(page_num).removeFirst();

            //now create a new temporary page table entry
            ptentry pte = opt_page_table.get(page_num);
            pte.setIndex(page_num);
            pte.setReferencedBit(true);

            //now check to see if we have to set a dirty bit
            if(line_conversion_array[1].equals("W")){
              pte.setDirtyBit(true);
            }
            //if the valid bit is true we have a hit, printout adress
            if(pte.getValidBit() == true){
              System.out.println(line_conversion_array[0] + " hit ");
            }
            else{
                //in this case we have a pagefault, you know what to do
                //when this comes around

                //in this case we have no eviction
                if(cur_frame < num_frames){
                  System.out.println(line_conversion_array[0] + " page fault - no eviction ");
                  page_fault_count++;
                  //set the current frame in the page_frame_array
                  page_frame_array[cur_frame] = page_num;

                  //set the properties of the page table entry
                  pte.setFrameNumber(cur_frame);
                  pte.setValidBit(true);
                  cur_frame++;
                }else{
                    //we have eviction

                    //create variable to determine longest distance
                    int longest_distance = findLongestDistancePage(page_frame_array,future);
                    ptentry temp_pte = opt_page_table.get(longest_distance);

                    //now check to see if dirty or clean
                    if(temp_pte.getDirtyBit() == true){
                      //show that we evict dirty
                      System.out.println(line_conversion_array[0] + " page fault - evict dirty ");
                      page_fault_count++;
                      disk_writes++;
                    }else{
                      //we evict clean
                      System.out.println(line_conversion_array[0] + " page fault - evict clean ");
                      page_fault_count++;
                    }

                    //now actually evict the page
                    page_frame_array[temp_pte.getFrameNumber()] = pte.getIndex();
                    pte.setFrameNumber(temp_pte.getFrameNumber());
                    //set the various bits for the page table entries
                    pte.setValidBit(true);

                    temp_pte.setValidBit(false);
                    temp_pte.setFrameNumber(-999);
                    temp_pte.setDirtyBit(false);
                    temp_pte.setReferencedBit(false);

                    opt_page_table.put(longest_distance,temp_pte);

                }
            }
            //now increment the memory accesses and put back into the page table
            opt_page_table.put(page_num, pte);
            memory_accesses++;

        }
}catch(Exception e){
  e.printStackTrace();
}
}

//now create the function to find the longest distance
public static int findLongestDistancePage(int[] page_frame_array, Hashtable<Integer, LinkedList<Integer>> future){
    int index = 0;
    int maximum = 0;

    //search through the pageframes array to the length
    for(int i = 0; i < page_frame_array.length; i++){
          //now check to see if the future page is empty
          if(future.get(page_frame_array[i]).isEmpty()){
              //get ut and return the page frame values
              return page_frame_array[i];

          }else{
              //this is the when we check against the maximum
              if(future.get(page_frame_array[i]).get(0) > maximum){
                //get the value from the front of the future array
                maximum = future.get(page_frame_array[i]).get(0);
                index = page_frame_array[i];

              }

          }


    }

    return index;

}



//in here we do the working set clock
public static void do_working_set_clock(){



  //begin by declaring an array of pageframes, this will be important for creating our memory
  int[] page_frame_array = new int[num_frames];
  int temp_page_fault_count = 0;

  //now create a buffered reader to get the files input
  BufferedReader br = null;

  //now a try catch for reading in the text
  try{
      System.out.println("Initilaizing");

      //now loop through and initialize the page table
      for(int i = 0; i<1024*1024; i++){
        ptentry temp_pte = new ptentry();
        //load the temporary page table entry into the clock array
        working_set_clock_pt.put(i,temp_pte);

      }

      //initialize the array of pageframes or something
      for(int i = 0; i < num_frames; i++){
          page_frame_array[i] = -1;
      }

      //create a variable to check what frame we are on,
      //also create a variable to check what instruction we are on
      int cur_frame = 0;
      int cur_instruction = 0;
      int refresh_counter = 0;

      //draw in a buffer to read the file
      br = new BufferedReader(new FileReader(trace_file));

      //read over the tracefile
      while(br.ready()){

          //now split the input line into it's parts
          String[] line_conversion_array = br.readLine().split(" ");

          //if the temp page count = tau
          if(temp_page_fault_count == tau_value){
            //reset the page fault value
              temp_page_fault_count = 0;
              page_fault_count = page_fault_count + tau_value;

          }

          //perform the refresh if need be
          if(refresh_counter == Integer.parseInt(refresh_value)){
              //do a refresh
              System.out.println("Refresh Occured");

              //iterate over the page array
              for(int i = 0; i < page_frame_array.length; i++){
                  ptentry refresh_temp_pte = working_set_clock_pt.get(page_frame_array[i]);
                  //refresh_temp_pte.setReferencedBit(true);
                  refresh_temp_pte.setAge(cur_instruction);
                  refresh_temp_pte.setTime(cur_instruction);
                  working_set_clock_pt.put(i,refresh_temp_pte);
              }

              //set the refresh counter back to 0
              refresh_counter = 0;

          }
          else{
            refresh_counter++;
          }





          //create a value for the page number
          int page_num = Integer.decode("0x" + line_conversion_array[0].substring(0,5));

          //now create a temorary page table entry for later use
          ptentry pte = working_set_clock_pt.get(page_num);
          //set the neccesary parameters for these ptes
          pte.setIndex(page_num);
          pte.setReferencedBit(true);

          //now check to see if we need to set a dirty bit
          if(line_conversion_array[1].equals("W")){
            pte.setDirtyBit(true);
          }

          //now check the valid bit
          if(pte.getValidBit() == true){
            //we have a hit
            System.out.println(line_conversion_array[0] + " hit ");
          }
          else{

                //it appears we have ourselves a page fault
                page_fault_count++;

                //in the case where we have free frames
                if(cur_frame < num_frames){

                  //printout the no eviction Message
                  System.out.println(line_conversion_array[0] + " page fault - no eviction");
                  //now set the values that need to be set
                  page_frame_array[cur_frame] = page_num;
                  pte.setFrameNumber(cur_frame);
                //  pte.setValidBit(true);
                  pte.setReferencedBit(true);
                  cur_frame++;

                }else{
                  //we have to evict

                  //integer to create which page number to evict
                  int page_number_to_evict = 0;
                  //see if I can find something to write to
                  boolean fflag = false;
                  page_fault_count++;

                  //loop over until something is found
                  while(fflag == false){

                  //move the clock pointer back to the begining
                  //if we hit the top
                    if(clock_pointer_position == page_frame_array.length || clock_pointer_position < 0){
                      clock_pointer_position = 0;
                    }


                    //evict this if it is found (rule 1)
                    //case where the dirty bit and referenced bit is true
                    if(working_set_clock_pt.get(page_frame_array[clock_pointer_position]).getReferencedBit() == false && working_set_clock_pt.get(page_frame_array[clock_pointer_position]).getDirtyBit() == false){
                      //if we find one with a bad referenced bit, evict
                        System.out.println("0x" + line_conversion_array[0] + " page fault - evict clean");
                      page_number_to_evict = page_frame_array[clock_pointer_position];
                      fflag = true;
                      temp_page_fault_count++;
                      page_fault_count++;
                      //this is for case 3 in the
                    }else if(working_set_clock_pt.get(page_frame_array[clock_pointer_position]).getReferencedBit() == false && working_set_clock_pt.get(page_frame_array[clock_pointer_position]).getDirtyBit() == true){
                        //now check to see if the frame is
                        //if we find one with a bad referenced bit, evict
                          System.out.println("0x" + line_conversion_array[0] + " page fault - evict dirty");
                        page_number_to_evict = page_frame_array[clock_pointer_position];
                        fflag = true;
                        disk_writes++;
                        temp_page_fault_count++;
                        page_fault_count++;
                    }
                    else{
                      //in the case that the refbit is true, set it to false
                      working_set_clock_pt.get(page_frame_array[clock_pointer_position]).setReferencedBit(false);

                    }

                    //now move the clock pointer forward
                    clock_pointer_position++;

                  }


                  //now in here do some stuff
                  ptentry temp_pte = working_set_clock_pt.get(page_number_to_evict);

                  //check to see if the page is dirty
                  if(temp_pte.getDirtyBit() == true){
                    System.out.println("0x" + line_conversion_array[0] + " page fault - evict dirty");
                    page_fault_count++;
                    disk_writes++;
                  }else{
                    System.out.println("0x" + line_conversion_array[0] + " page fault - evict clean");
                    page_fault_count++;
                  }

                  //now evict or swap out page
                  if(temp_pte.getFrameNumber() != -1){
                  page_frame_array[temp_pte.getFrameNumber()] = pte.getIndex();
                  }
                  pte.setFrameNumber(temp_pte.getFrameNumber());
                  pte.setValidBit(true);

                  //now set the values for the temp pte
                  temp_pte.setDirtyBit(false);
                  temp_pte.setReferencedBit(false);
                  temp_pte.setValidBit(false);
                  temp_pte.setFrameNumber(-1);
                  working_set_clock_pt.put(page_number_to_evict,temp_pte);

                  page_fault_count++;
                }
          }
//now what you want to do here is put values into the page table
working_set_clock_pt.put(page_num, pte);
cur_instruction++;
memory_accesses++;
}

//place catch here
  }catch(IOException e){
    System.out.println("Message Error: " + e.getMessage());
  }






}











//this function populates the clock list with values that
//are invalid adresses to show that the adresses are invalid so that when itterating through
//the physical_memory we are in good shape.
public static void populate_clock_list(){
      //creating an invalid page frame
      pageFrame pfNew = new pageFrame();
      pfNew.setValidBit(false);
      //run over the list creating invalid entries for the main memory
      //so that when they are encountered we know that the frame in memory is
      //free
      int x = 0;
      //print out what the xvalues is and the numframes size
    //  System.out.println("intx: " + x);
    //  System.out.println("num_frames: " + num_frames);
      while(x < num_frames){
        clock_physical_memory.add(pfNew);
        x++;
      }
    //  System.out.println("Null Memory Populated");

}

public static void populate_aging_list(){
      //creating an invalid page frame
      pageFrame pfNew = new pageFrame();
      pfNew.setValidBit(false);
      //run over the list creating invalid entries for the main memory
      //so that when they are encountered we know that the frame in memory is
      //free
      int x = 0;
      //print out what the xvalues is and the numframes size
    //  System.out.println("intx: " + x);
    //  System.out.println("num_frames: " + num_frames);
      while(x < num_frames){
        aging_physical_memory.add(pfNew);
        x++;
      }
    //  System.out.println("Null Memory Populated");

}

public static void do_aging_algorithm(){
  //a counter to keep track of when refreshes are needed
  int refresh_counter = 0;
  String line_string = "";
  //starting off similar to clock by reading in a fileString rw_string = p1.getStr2();String rw_string = p1.getStr2();
  try{
      //open up the file
        FileInputStream fstream = new FileInputStream(trace_file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
        String strLine = "";

        //start reading the trace file line by line in this loop


        while ((strLine = br.readLine()) != null){
          //pass the strline into a function that will split it(funtion returns a pair object)
          Pair p1 = line_split(strLine);
          //now separate out the strLine into it's 2 components the adress string and the read write string
          String adress_string = p1.getStr1();
          line_string = "0x" + adress_string;
          String rw_string = p1.getStr2();
          //need to covert the adress_string so we know what page we are on
          adress_string = Long.toHexString(convert_to_base_adress(Long.parseLong(adress_string,16)));

        //  System.out.println("Refresh Value: " + Integer.parseInt(refresh_value));
        //  System.out.println("Refresh Counter: " + refresh_counter);
          //check to see if a refresh needs to be done on the memory
          if(refresh_counter == Integer.parseInt(refresh_value)){
            refresh_counter = 0;
            System.out.println("Memory Refresh");
            refresh_memory();
          }
          else{
            refresh_counter++;
          }

          //some printlns for debugging purposes
        //  System.out.println("adress_string: " + adress_string);
        //  System.out.println("rw_string: " + rw_string);


          //here is the loop where the actual implementation is done
          //remember to break out whenever a new instruction is required
          while(true){
                //a simple println to check the size of the virtual memory
              //  System.out.println("aging table size: " + aging_page_table.size());

                  //first check when to evict
                  //in this algorithm we must evict on a page miss
                  //and whenever the memory is full
                  if(aging_page_table.size() == num_frames  && aging_page_table.get(adress_string) == null){
                    String evictString = "";
                  //  System.out.println("Page Fault Memory Full");
                    //itterate through the actual memory and find where the place with
                    //the lowest value for the age is found and then evict that
                    int lowest_value_location = 0;
                    int lowest_value = 999;
                    int itterator = 0;
                    int current_value = 0;

                    while(itterator < num_frames){
                          //pull out the page frame from the memory
                          pageFrame temporaryPF = aging_physical_memory.get(itterator);
                          if(temporaryPF.getAge() < lowest_value && temporaryPF.getReferencedBit() != true){
                            lowest_value = temporaryPF.getAge();
                            lowest_value_location = itterator;




                          }

                          //check to see if this new low is dirty or not
                          if(temporaryPF.getDirtyBit() == true){
                            evictString = "dirty";
                            dirty_tag = true;
                          }else{
                            evictString = "clean";
                            dirty_tag = false;
                          }

                      itterator++;
                    }

                    //now evict the worst value from the page table
                    aging_page_table.remove(aging_physical_memory.get(lowest_value_location).getPageTableEntryAdress());


                    //now create a new page frame and put it into the memory
                    pageFrame newpf = new pageFrame();
                    newpf.setReferencedBit(true);
                    newpf.setValidBit(true);
                    newpf.setPageTableEntryAdress(adress_string);
                    aging_physical_memory.set(lowest_value_location,newpf);

                    //now check what to increment based on what the dirty tag was
                    if(dirty_tag){
                      disk_writes++;
                    }
                    page_fault_count++;


                    //also add the value to the page table
                    ptentry new_ptentry = new ptentry();
                    new_ptentry.setAdress(adress_string);
                    new_ptentry.setFrameNumber(lowest_value_location);
                    //now add the new page table entry to the page table
                    aging_page_table.put(new_ptentry.getAdress(),lowest_value_location);



                    System.out.println(line_string + " Page Fault - Evict " + evictString);

                    //must increment the memory iterator to know where
                    //the next free space will be
                    increment_memory_iterator();

                    //after evicting and putting something back
                    //it is time to break and bring in a new instruction
                    break;
                  }



                  //case 1 when there is an available free frame and there is no need to evict
                  if(aging_physical_memory.get(memory_itterator).getValidBit() == false && aging_page_table.get(adress_string) == null){

                    //create a new page frame to go into the physical memory
                    pageFrame newpf = new pageFrame();
                    newpf.setReferencedBit(true);
                    newpf.setValidBit(true);
                    newpf.setPageTableEntryAdress(adress_string);
                    //add this to the phyisical memory
                    aging_physical_memory.set(memory_itterator,newpf);

                    //now create a new entry for the page table
                    ptentry new_ptentry = new ptentry();
                    new_ptentry.setAdress(adress_string);
                    new_ptentry.setFrameNumber(memory_itterator);
                    //now add the new page table entry to the page table
                    aging_page_table.put(new_ptentry.getAdress(),memory_itterator);


                    //break out as there is a page fault but no eviction
                    System.out.println(line_string + " Page Fault - No Eviction");
                    //must increment the memory iterator to know where
                    //the next free space will be
                    increment_memory_iterator();
                    break;
                  }

                  //in the case that the value is already loaded into memory
                  //generate a page hit
                  if(aging_page_table.get(adress_string) != null){
                    //  System.out.println("Temp page table entry adress: " + temp_pte.getAdress());

                      //pull out a frame from the physical memory the pageFrame to check the dirty bits
                      int location = aging_page_table.get(adress_string);
                      pageFrame temporaryPF = aging_physical_memory.get(location);

                      //now check to see if the temporaryPF is clean or dirty
                      if(temporaryPF.getDirtyBit() == false && rw_string.equals("W")){
                        temporaryPF.setDirtyBit(true);
                      }

                      //remember to set the referenced bit to true in this case because
                      //it will need to be known that this is referenced bit to true
                    //  System.out.println("Referenced Bit Reset");
                      temporaryPF.setReferencedBit(true);

                      //put the temporary page frame back into memory with the modified bit if needed
                      aging_physical_memory.set(location,temporaryPF);

                      System.out.println(line_string + " Hit");
                      memory_accesses++;
                      break;
                    }



                  break;
          }

          //just some extra space so output is readable
        //  System.out.println("\n");





        }


    }catch(IOException e){
      System.out.println("Message Error: " + e.getMessage());
    }







}

//this function is meant to increment the memory increment_memory_iterator
public static void increment_memory_iterator(){
    //System.out.println("Incrementing Memory Itterator");
  //  System.out.println("Current Itterator Value: " + memory_itterator);

  //now increment the memory itterator unless it is equal to the page frames
  if(memory_itterator == num_frames - 1){
    memory_itterator = 0;
  }else{
  memory_itterator++;
}


}


//this function here refreshes the memory for the aging algorithm
public static void refresh_memory(){
    //System.out.println("Refreshing Memory");

    //itterate over the memory and act based on what the refbits are
    int itterator = 0;
    while(itterator < num_frames){
      pageFrame tempPF = aging_physical_memory.get(itterator);
      memory_accesses++;
      //in the case that the referenced bit is 1, set it to zero and add 128
      // to 1/2 of what is currently in the age
      if(tempPF.getReferencedBit() == true){
        //reset the referenced_bit
        tempPF.setReferencedBit(false);
        //increment the age
        tempPF.setAge((tempPF.getAge()/2) + 128);
        //then put the value back into the memory
        aging_physical_memory.set(itterator, tempPF);
      }else{
        //in the case that the referenced bit is false
        //reset the referenced_bit
        tempPF.setReferencedBit(false);
        //split the age in half
        tempPF.setAge((tempPF.getAge()/2));
        //then put the value back into the memory
        aging_physical_memory.set(itterator, tempPF);
      }

      itterator++;

    }
}




//this function implements the clock algorithm
//this is a second chance replacement algorithm that changes bits
//in order to do eviction. it itterates through if all of the frame are full
//and trys to evict something with a referenced bit that is 0
public static void do_clock_algorithm(){

  //premtively set the temp_frame_number and current_frame_number
  current_frame_number = 0;
  temp_frame_number = 0;

  String adress_string_2 = "";


  try{
      //open up the file
        FileInputStream fstream = new FileInputStream(trace_file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
        String strLine = "";
        //start reading the trace file line by line in this loop
        while ((strLine = br.readLine()) != null){
          //pass the strline into a function that will split it(funtion returns a pair object)
          Pair p1 = line_split(strLine);
          //now separate out the strLine into it's 2 components
          String adress_string = p1.getStr1();
          adress_string_2 = adress_string;
          //need to covert the adress_string so we know what page we are on
          adress_string = Long.toHexString(convert_to_base_adress(Long.parseLong(adress_string,16)));

            //now that we have the two components of this we can create a page table Entry
            //if the rw string comes in as a w we must create the ptentry with a dirty bit of 1
            String rw_string = p1.getStr2();

            //create the page table Entry
            ptentry temp_pte = new ptentry();

            //need to figure out what page that needs to go in
            temp_pte.setAdress(adress_string);

            //now check to see if there is a place to put this into a frame
            int free_frame_number = 0;




            //now itterate through the linkedlist representing real
            //memory looking for a free frame. we will break out of this
            //loop whenever something gets put into memory either through
            //eviction or through just normal placement
            while(true){

                  //before hand, tell me where the temp frame number is and the  current_frame_number are
                //  System.out.println("current_frame_number: " + current_frame_number);
                //  System.out.println("temp_frame_number: " + temp_frame_number);
                //  System.out.println("Page Table Size: " + clock_page_table.size());

                  //check to see if the current frame is empty
                  //if the adress in there is negative 1 we know that the
                  //frame is empty, also check to see that it is not in the
                  //page table entry already(case 1)
                  if(clock_physical_memory.get(temp_frame_number).getValidBit() == false && clock_page_table.get(temp_pte.getAdress()) == null){
                        //set the frame number of the page table entry
                        temp_pte.setFrameNumber(temp_frame_number);




                        //now add the page table entry to the hash table
                        clock_page_table.put(temp_pte.getAdress(),temp_pte.getFrameNumber());

                        //now add the new frame to memory
                        pageFrame newPageFrame = new pageFrame();

                        //if we have a w we need to set the dirty bit to true
                        if(rw_string.equals("W")){
                          //  System.out.println("This is a write: setting dirty bit");
                            newPageFrame.setDirtyBit(true);
                        }
                        else{
                          //  System.out.println("This is not a write: keep dirtybit false");
                            newPageFrame.setDirtyBit(false);
                        }

                        //must set the referenced and valid bits beforhand
                        //set referenced bit to true because it is our first insert into the frame
                        //System.out.println("referenced_bit is set to true");
                        newPageFrame.setReferencedBit(true);
                        newPageFrame.setValidBit(true);

                        //set the adress for the new page frame
                        newPageFrame.setPageTableEntryAdress(temp_pte.getAdress());


                        //now add the page frame to the physical memory at the adress where the empty frame was
                        clock_physical_memory.set(temp_frame_number,newPageFrame);

                        System.out.println("0x" + adress_string_2 + " Page Fault- No Eviction");

                        //increment the page fault count
                        page_fault_count++;
                        memory_accesses++;


                        increment_temporary_frame_number();

                        //break out to draw in the next instruction when this is encountered
                        break;

                  }

                  //System.out.println("This was hit after the break:");
                  //this case is case 2 where the value is already in the virtual memory
                  //and therefore is in physical memory as well, what we do here is check the bits and see
                  //if what is in the main memory is dirty or clean, we do not have a page fault in this case but
                  //we may have to change a dirty bits
                  if(clock_page_table.get(temp_pte.getAdress()) != null){
                    //  System.out.println("Temp page table entry adress: " + temp_pte.getAdress());

                      //pull out from physical memory the pageFrame to check the dirty bits
                      pageFrame temporaryPF = clock_physical_memory.get(temp_pte.getFrameNumber());

                      //now check to see if the temporaryPF is clean or dirty
                      if(temporaryPF.getDirtyBit() == false && rw_string.equals("W")){
                        temporaryPF.setDirtyBit(true);
                      }

                      //remember to set the referenced bit to true in this case because
                      //it will need to be known that this is referenced bit to true
                    //  System.out.println("Referenced Bit Reset");
                      temporaryPF.setReferencedBit(true);

                      //put the temporary page frame back into memory with the modified bit if needed
                      clock_physical_memory.set(temp_pte.getFrameNumber(),temporaryPF);

                      memory_accesses++;
                      System.out.println("0x" + adress_string_2 + " hit");

                    //once again break out so that we can draw in the next line
                    break;
                  }



                //now check to see if something needs to be evicted
                //this is case 3, check to see if the value is not in the page table
                //then the valid bit is false
                if( clock_page_table.get(temp_pte.getAdress()) == null && clock_physical_memory.get(temp_frame_number).getValidBit() == true && clock_physical_memory.get(temp_frame_number).getReferencedBit() == true){
                    //create a new pageframe to insert
                    pageFrame newTempPF = clock_physical_memory.get(temp_frame_number);


                    //set the referenced bit to false because we hit something that already was referenced
                    //but got hit again so the bit got set to false
                    newTempPF.setReferencedBit(false);

                    //add the page frame to the physical_memory
                    clock_physical_memory.set(temp_frame_number,newTempPF);

                    //System.out.println("Referenced Bit set to False");

                    memory_accesses++;
                    //do not break out because it is not required because
                    //we have not found a place for our frame yet but we still need to increment the
                    //temp location value
                    increment_temporary_frame_number();
                    System.out.println();

                }


                //now for case 4 which is the eviction case, this is when a pages referenced bit
                //is false and an eviction must occur
                //it is similar to case three except in this case we do an eviction and actuall place
                //what we need to into memory both virtual and physical after performing an eviction
                  if( clock_page_table.get(temp_pte.getAdress()) == null && clock_physical_memory.get(temp_frame_number).getReferencedBit() == false){


                        //now evict the original from the Hash table
                        clock_page_table.remove(clock_physical_memory.get(temp_frame_number).getPageTableEntryAdress());

                      //if the value in the physical memory was dirty then a write to disk occured
                      if(clock_physical_memory.get(temp_frame_number).getDirtyBit() == true){
                        System.out.println("0x" + adress_string_2 + " Page Fault - Evict Dirty");

                        //now increment the the disk writeout counter
                        disk_writes++;

                      }
                      else{
                          //otherwise the pagefault evicted clean
                          //and we are good
                          System.out.println("0x" + adress_string_2 + " Page Fault - Evict Clean");

                      }
                      //set the value of the temp page table entry so that it can be inserted into the VM
                      temp_pte.setFrameNumber(temp_frame_number);

                      //now set the values in and the physical_memory
                      pageFrame pf = clock_physical_memory.get(temp_frame_number);
                      //set the new values for the page frame
                      pf.setReferencedBit(true);
                      //remember to set the adress of the new pageFrame
                      pf.setPageTableEntryAdress(temp_pte.getAdress());
                      //now see if you must set the dirty bit
                      if(rw_string.equals("w")){
                        pf.setDirtyBit(true);
                      }





                      //now add the temp paget table entry into the page table and physical memory
                      clock_page_table.put(temp_pte.getAdress(),temp_pte.getFrameNumber());
                      clock_physical_memory.set(temp_frame_number,pf);
                      //increase the number of memory accesses here because we accessed memory
                      memory_accesses++;
                      page_fault_count++;

                      //also must increment the temporary frame number
                      increment_temporary_frame_number();
                    //break out because now we can allocate this to the physical and virtual memory
                    break;
                  }







            }



            //this is here just to check to see what is going into the
            //VM and the physical Memory

            //print out the address and rw strings for testing purposes
            //System.out.println("Adress String: " + adress_string);
            //System.out.println("Read/Write String: " + rw_string);
            //System.out.println("\n\n");

          }




    }catch(IOException e){
      System.out.println("Message Error: " + e.getMessage());
    }

}


//these two fuctions keep track of the current and temorary frame
//number for the clock algorithm
public static void increment_temporary_frame_number(){
      if(temp_frame_number + 1 == num_frames){
        temp_frame_number = 0;
        return;
      }
      temp_frame_number++;

}

public static void set_current_frame_number(int x){

    current_frame_number = x;

}






//now construct the memory by adding 1000 for every page
public static void construct_physical_memory(){
  //take the value for number of pages and set it to the max
  physical_memory = new HashMap();
}







//output the statistics from runtime into the screen
  public static void output_stats(){
          System.out.println("Total Memory Accesses: " + memory_accesses);
          System.out.println("Total Page Faults: " + page_fault_count);
          System.out.println("Total Writes to Disk: " + disk_writes);


  }

//a function that will split a string into it
public static Pair line_split(String s){

    String str1 = "";
    String str2 = "";
    Pair output_pair = new Pair();
    int x = 0;
    //run through string s until a space is found
    while(s.charAt(x) != ' '){
      str1 = str1 + s.charAt(x);
      x++;
    }
    x++;
    //now check to get the last integer out of the string
    while(x < s.length()){
      str2 = str2 + s.charAt(x);
      x++;
    }

  //  System.out.println("STR1: " + str1);
  //  System.out.println("STR2: " + str2);

    //set the results to the output Pair
    output_pair.setStr1(str1);
    output_pair.setStr2(str2);



  return output_pair;
}

//this function generates the virtual memory
//dont bother with this it is not necessary
public static void construct_virtual_memory(){
      //use a list to add these values


}

//populate the optimal linked list
public static void populate_opt_list(){
  try{
      //open up the file
        FileInputStream fstream = new FileInputStream(trace_file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
        String strLine = "";
        int x = 0;
        System.out.println("Populating Adress List For Opt");
//now itterate over the file
  while ((strLine = br.readLine()) != null)   {


    //add the address from the pair to the optimal history file so that we can remember
    //the order in which things are added
          strLine = 0 + strLine;


    //create a temporary pair to store the split strings
        Pair temp_split_double_string = new Pair();

          //split the str line into 2 strings
        temp_split_double_string = line_split(strLine);




        opt_history.add(temp_split_double_string.getStr1());
        System.out.println("Value inside Opt history: " + opt_history.get(x));
        x++;
      }


      }
      catch(IOException e){
        System.out.println("Message Error: " + e.getMessage());
      }

}




//this splits out the bits for the string that is input
//it takes a string in hex form as an input value and returns
//a PTLocation which has data that can be used in a ptetry
public static PTLocation bitsplitter(String hex_value){
        //take the hex string and turn it into a value
        //get the input from the parameters
        long int_string_value = Long.parseLong(hex_value, 16);
        //System.out.println("int string value: " + int_string_value);
        String bit_string = Long.toBinaryString(int_string_value);

        PTLocation ptloc = new PTLocation();

        //make sure the length of the bit string is equal to 32
        while(bit_string.length() != 32){
          bit_string = 0 + bit_string;
        }

        //get out the initial string
        String first_level = bit_string.substring(0,9);
        String second_level = bit_string.substring(10,19);
        String offset_string = bit_string.substring(20,31);

        //put the values into the ptloc
        ptloc.setLevel1String(first_level);
        ptloc.setLevel2String(second_level);
        ptloc.setOffset(offset_string);
        System.out.println(bit_string);

        //now start to split the bit string
        System.out.println("firstlevel: " + ptloc.getLevel1String());
        System.out.println("secondLevel: " + ptloc.getLevel2String());
        System.out.println("Offset: " + ptloc.getOffsetString());


        return ptloc;

      }



//this function basically converts an adress for
//the use
public static long convert_to_base_adress(long x){
      x = x / 4096;
      x = x * 4096;

      return x;


}

//this is a good selector for the parameters
public static void parameter_selector(String s, String[] args){
    //checkout parameters
    System.out.println("Param 1: " + args[1]);
    System.out.println("Param 2: " + args[2]);
    System.out.println("Param 3: " + args[3]);
  //  System.out.println("Param 4: " + args[4]);
  //  System.out.println("Param 5: " + args[5]);
//    System.out.println("Param 6: " + args[6]);

    //
    //pull in the nuber of frames because it is required by all of the
    //the required methods
    num_frames = Integer.parseInt(args[1]);


//in the case of opt, draw in these parameters
      if(s.equals("opt")){
        //select out the parameter for opt
        trace_file = args[4];
      }
      //for the clock case draw in these parameters
      if(s.equals("clock")){
        //select out the parameter for opt
        trace_file = args[4];


      }
      //select in certain parameters for
      //the working set algorithm
      if(s.equals("work")){
        //pull out the tau value
        refresh_value = args[5];
        tau_value = Integer.parseInt(args[7]);
        trace_file = args[8];


      }
      //select in the correct parameters whenever there
      //is the aging algorithmi
      if(s.equals("aging")){
        refresh_value = args[5];
        trace_file = args[6];

      }

}

}
