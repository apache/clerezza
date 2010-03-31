
/* First created by JCasGen Wed Mar 31 16:02:30 CEST 2010 */
package org.apache.uima.calais;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Wed Mar 31 16:02:30 CEST 2010
 * @generated */
public class BaseType_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (BaseType_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = BaseType_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new BaseType(addr, BaseType_Type.this);
  			   BaseType_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new BaseType(addr, BaseType_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = BaseType.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.apache.uima.calais.BaseType");
 
  /** @generated */
  final Feature casFeat_calaisType;
  /** @generated */
  final int     casFeatCode_calaisType;
  /** @generated */ 
  public String getCalaisType(int addr) {
        if (featOkTst && casFeat_calaisType == null)
      jcas.throwFeatMissing("calaisType", "org.apache.uima.calais.BaseType");
    return ll_cas.ll_getStringValue(addr, casFeatCode_calaisType);
  }
  /** @generated */    
  public void setCalaisType(int addr, String v) {
        if (featOkTst && casFeat_calaisType == null)
      jcas.throwFeatMissing("calaisType", "org.apache.uima.calais.BaseType");
    ll_cas.ll_setStringValue(addr, casFeatCode_calaisType, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public BaseType_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_calaisType = jcas.getRequiredFeatureDE(casType, "calaisType", "uima.cas.String", featOkTst);
    casFeatCode_calaisType  = (null == casFeat_calaisType) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_calaisType).getCode();

  }
}



    