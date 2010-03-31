

/* First created by JCasGen Wed Mar 31 16:02:30 CEST 2010 */
package org.apache.uima.calais;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Wed Mar 31 16:02:30 CEST 2010
 * @generated */
public class BaseType extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(BaseType.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected BaseType() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public BaseType(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public BaseType(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public BaseType(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: calaisType

  /** getter for calaisType - gets OpenCalais type
   * @generated */
  public String getCalaisType() {
    if (BaseType_Type.featOkTst && ((BaseType_Type)jcasType).casFeat_calaisType == null)
      jcasType.jcas.throwFeatMissing("calaisType", "org.apache.uima.calais.BaseType");
    return jcasType.ll_cas.ll_getStringValue(addr, ((BaseType_Type)jcasType).casFeatCode_calaisType);}
    
  /** setter for calaisType - sets OpenCalais type 
   * @generated */
  public void setCalaisType(String v) {
    if (BaseType_Type.featOkTst && ((BaseType_Type)jcasType).casFeat_calaisType == null)
      jcasType.jcas.throwFeatMissing("calaisType", "org.apache.uima.calais.BaseType");
    jcasType.ll_cas.ll_setStringValue(addr, ((BaseType_Type)jcasType).casFeatCode_calaisType, v);}    
  }

    