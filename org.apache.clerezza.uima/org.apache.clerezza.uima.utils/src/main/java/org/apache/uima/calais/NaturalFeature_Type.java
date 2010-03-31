
/* First created by JCasGen Wed Mar 31 16:02:30 CEST 2010 */
package org.apache.uima.calais;

import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;

/** 
 * Updated by JCasGen Wed Mar 31 16:02:30 CEST 2010
 * @generated */
public class NaturalFeature_Type extends BaseType_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (NaturalFeature_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = NaturalFeature_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new NaturalFeature(addr, NaturalFeature_Type.this);
  			   NaturalFeature_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new NaturalFeature(addr, NaturalFeature_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = NaturalFeature.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.apache.uima.calais.NaturalFeature");



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public NaturalFeature_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

  }
}



    