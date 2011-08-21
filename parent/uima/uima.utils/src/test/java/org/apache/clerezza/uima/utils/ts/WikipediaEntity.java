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

/* First created by JCasGen Thu Jul 21 16:48:14 CEST 2011 */
package org.apache.clerezza.uima.utils.ts;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** an Entity from Wikipedia
 * Updated by JCasGen Thu Jul 21 16:48:14 CEST 2011
 * XML source: /Users/tommasoteofili/Documents/workspaces/clerezza_workspace/trunk/parent/uima/uima.utils/src/test/resources/ClerezzaTestTypeSystemDescriptor.xml
 * @generated */
public class WikipediaEntity extends ClerezzaBaseEntity {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(WikipediaEntity.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected WikipediaEntity() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public WikipediaEntity(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public WikipediaEntity(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: name

  /** getter for name - gets 
   * @generated */
  public String getName() {
    if (WikipediaEntity_Type.featOkTst && ((WikipediaEntity_Type)jcasType).casFeat_name == null)
      jcasType.jcas.throwFeatMissing("name", "org.apache.clerezza.uima.utils.ts.WikipediaEntity");
    return jcasType.ll_cas.ll_getStringValue(addr, ((WikipediaEntity_Type)jcasType).casFeatCode_name);}
    
  /** setter for name - sets  
   * @generated */
  public void setName(String v) {
    if (WikipediaEntity_Type.featOkTst && ((WikipediaEntity_Type)jcasType).casFeat_name == null)
      jcasType.jcas.throwFeatMissing("name", "org.apache.clerezza.uima.utils.ts.WikipediaEntity");
    jcasType.ll_cas.ll_setStringValue(addr, ((WikipediaEntity_Type)jcasType).casFeatCode_name, v);}    
   
    
  //*--------------*
  //* Feature: category

  /** getter for category - gets 
   * @generated */
  public String getCategory() {
    if (WikipediaEntity_Type.featOkTst && ((WikipediaEntity_Type)jcasType).casFeat_category == null)
      jcasType.jcas.throwFeatMissing("category", "org.apache.clerezza.uima.utils.ts.WikipediaEntity");
    return jcasType.ll_cas.ll_getStringValue(addr, ((WikipediaEntity_Type)jcasType).casFeatCode_category);}
    
  /** setter for category - sets  
   * @generated */
  public void setCategory(String v) {
    if (WikipediaEntity_Type.featOkTst && ((WikipediaEntity_Type)jcasType).casFeat_category == null)
      jcasType.jcas.throwFeatMissing("category", "org.apache.clerezza.uima.utils.ts.WikipediaEntity");
    jcasType.ll_cas.ll_setStringValue(addr, ((WikipediaEntity_Type)jcasType).casFeatCode_category, v);}    
  }

    