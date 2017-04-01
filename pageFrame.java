public class pageFrame{

  public boolean dirty_bit = false;
  public boolean referenced_bit = false;
  public boolean valid_bit = false;
  public String pte_adress = "Invalid";
  public long pte_adress_long = 0;
  public int age = 0;

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
