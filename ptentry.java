public class ptentry{


  public String adress = "";
  public long long_adress = 0;
  public int frame_number = 0;
  public int index = 0;
  public boolean dirty_bit = false;
  public boolean referenced_bit = false;
  public boolean valid_bit = false;
  public String pte_adress = "Invalid";
  public long pte_adress_long = 0;
  public int age = 0;
  public int time = 0;

  //now for the setters & getters or
  //mutators and retrieiers

  public int getFrameNumber(){
    return frame_number;
  }
  public int getTime(){
    return frame_number;
  }
  public int getIndex(){
    return index;
  }
  public String getAdress(){
    return adress;
  }
  public void setAdress(String s){
    adress = s;
    long_adress = Long.parseLong(s,16);
  }
  public long getAdressAsLong(){
    return long_adress;
  }
  public void setFrameNumber(int x){
    frame_number = x;
  }
  public void setTime(int x){
    time = x;
  }
  public void setIndex(int x){
    index = x;
  }


//extra functions copied over from page frame because they are useful here
//now for the getters and setters for the various elements
public boolean getDirtyBit(){
  return dirty_bit;
}
public boolean getReferencedBit(){
  return referenced_bit;
}
public String getPageTableEntryAdress(){
  return pte_adress;
}
public int getAge(){
  return age;
}
public void setDirtyBit(boolean b){
  dirty_bit = b;
}
public void setReferencedBit(boolean b){
  referenced_bit = b;
}
public void setPageTableEntryAdress(String s){
  pte_adress = s;
  //set the integer value for the page table entry for faster comparasons
  pte_adress_long = Long.parseLong(s,16);
}
public void setValidBit(boolean b){
  valid_bit = b;
}
public void setAge(int x){
  age = x;
}
public boolean getValidBit(){
  return valid_bit;
}


}
