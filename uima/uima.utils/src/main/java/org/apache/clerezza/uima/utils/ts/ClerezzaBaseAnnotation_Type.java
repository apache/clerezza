/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/* First created by JCasGen Wed Jul 06 14:49:10 CEST 2011 */
package org.apache.clerezza.uima.utils.ts;

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
 * Updated by JCasGen Sun Aug 21 14:41:29 CEST 2011
 * @generated */
public class ClerezzaBaseAnnotation_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
               if (ClerezzaBaseAnnotation_Type.this.useExistingInstance) {
                 // Return eq fs instance if already created
               FeatureStructure fs = ClerezzaBaseAnnotation_Type.this.jcas.getJfsFromCaddr(addr);
               if (null == fs) {
                 fs = new ClerezzaBaseAnnotation(addr, ClerezzaBaseAnnotation_Type.this);
                 ClerezzaBaseAnnotation_Type.this.jcas.putJfsFromCaddr(addr, fs);
                 return fs;
               }
               return fs;
        } else return new ClerezzaBaseAnnotation(addr, ClerezzaBaseAnnotation_Type.this);
        }
    };

  /** @generated */
  public final static int typeIndexID = ClerezzaBaseAnnotation.typeIndexID;

  /**
   * @generated
   * @modifiable
   */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.apache.clerezza.uima.utils.ts.ClerezzaBaseAnnotation");

  /** @generated */
  final Feature casFeat_uri;

  /** @generated */
  final int casFeatCode_uri;

  /** @generated */
  public String getUri(int addr) {
        if (featOkTst && casFeat_uri == null)
      jcas.throwFeatMissing("uri", "org.apache.clerezza.uima.utils.ts.ClerezzaBaseAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_uri);
  }
  /** @generated */
  public void setUri(int addr, String v) {
        if (featOkTst && casFeat_uri == null)
      jcas.throwFeatMissing("uri", "org.apache.clerezza.uima.utils.ts.ClerezzaBaseAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_uri, v);}
    
  



  /**
   * initialize variables to correspond with Cas Type and Features
   * 
   * @generated
   */
  public ClerezzaBaseAnnotation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_uri = jcas.getRequiredFeatureDE(casType, "uri", "uima.cas.String", featOkTst);
    casFeatCode_uri  = (null == casFeat_uri) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_uri).getCode();

  }
}
