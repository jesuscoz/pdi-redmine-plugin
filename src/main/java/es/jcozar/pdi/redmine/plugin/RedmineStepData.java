/*! ******************************************************************************
*
* Pentaho Data Integration
*
* Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
*
*******************************************************************************
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
******************************************************************************/

package es.jcozar.pdi.redmine.plugin;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * keep track of per-thread resources during step execution.
 */
public class RedmineStepData extends BaseStepData implements StepDataInterface {

	public RowMetaInterface inputRowMeta;
	
	public int indexOfSubjectField;
	public int indexOfDescriptionField;
	public int indexOfAssignedToField;
	public int indexOfAttachedFileFilename;
	
	public RedmineStepData() {
		super();
	}
}
