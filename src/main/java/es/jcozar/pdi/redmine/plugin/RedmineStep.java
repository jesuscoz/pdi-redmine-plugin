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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import com.taskadapter.redmineapi.ProjectManager;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueCategoryFactory;
import com.taskadapter.redmineapi.bean.UserFactory;

/**
 * The step will receive data and will create a redmine issue based
 * on the Meta Step definition.
 */

public class RedmineStep extends BaseStep implements StepInterface {

	private static final Class<?> PKG = RedmineStep.class; // for i18n purposes
	
	/**
	 * The constructor should simply pass on its arguments to the parent class.
	 * 
	 * @param s                 step description
	 * @param stepDataInterface step data class
	 * @param c                 step copy
	 * @param t                 transformation description
	 * @param dis               transformation executing
	 */
	public RedmineStep(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
		super(s, stepDataInterface, c, t, dis);
	}

	/**
	 * This method is called by PDI during transformation startup.
	 * 
	 * It should initialize required for step execution.
	 * 
	 * The meta and data implementations passed in can safely be cast to the step's
	 * respective implementations.
	 * 
	 * It is mandatory that super.init() is called to ensure correct behavior.
	 * 
	 * Typical tasks executed here are establishing the connection to a database, as
	 * wall as obtaining resources, like file handles.
	 * 
	 * @param smi step meta interface implementation, containing the step settings
	 * @param sdi step data interface implementation, used to store runtime
	 *            information
	 * 
	 * @return true if initialization completed successfully, false if there was an
	 *         error preventing the step from working.
	 * 
	 */
	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		// Casting to step-specific implementation classes is safe
		RedmineStepMeta meta = (RedmineStepMeta) smi;
		RedmineStepData data = (RedmineStepData) sdi;
		if (!super.init(meta, data)) {
			return false;
		}

		// no work needed
		return true;
	}

	/**
	 * Once the transformation starts executing, the processRow() method is called
	 * repeatedly by PDI for as long as it returns true. To indicate that a step has
	 * finished processing rows this method must call setOutputDone() and return
	 * false;
	 * 
	 * Steps which process incoming rows typically call getRow() to read a single
	 * row from the input stream, change or add row content, call putRow() to pass
	 * the changed row on and return true. If getRow() returns null, no more rows
	 * are expected to come in, and the processRow() implementation calls
	 * setOutputDone() and returns false to indicate that it is done too.
	 * 
	 * Steps which generate rows typically construct a new row Object[] using a call
	 * to RowDataUtil.allocateRowData(numberOfFields), add row content, and call
	 * putRow() to pass the new row on. Above process may happen in a loop to
	 * generate multiple rows, at the end of which processRow() would call
	 * setOutputDone() and return false;
	 * 
	 * @param smi the step meta interface containing the step settings
	 * @param sdi the step data interface that should be used to store
	 * 
	 * @return true to indicate that the function should be called again, false if
	 *         the step is done
	 */
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {

		// safely cast the step settings (meta) and runtime info (data) to specific
		// implementations
		RedmineStepMeta meta = (RedmineStepMeta) smi;
		RedmineStepData data =  (RedmineStepData) sdi;

		// get incoming row, getRow() potentially blocks waiting for more rows, returns
		// null if no more rows expected
		Object[] r = getRow();

		// if no more rows are expected, indicate step is finished and processRow()
		// should not be called again
		if (r == null) {
			setOutputDone();
			return false;
		}
		
		if ( first ) {
			
			first = false;
			data.inputRowMeta = getInputRowMeta();
			
			if(meta.isRedmineSubjectInField()) {
				String realSubjectfieldName = environmentSubstitute( meta.getRedmineSubjectField() );
				data.indexOfSubjectField = data.inputRowMeta.indexOfValue( ( realSubjectfieldName ) );
				if ( data.indexOfSubjectField < 0 ) {
		            // The field is unreachable !
		            logError( BaseMessages.getString( PKG, "RedmineStep.Error.ErrorFindingField", realSubjectfieldName ) );
		            throw new KettleException( BaseMessages.getString( PKG, "RedmineStep.Error.ErrorFindingField", realSubjectfieldName ) );
				}
			}
			
			if (meta.isRedmineDescriptionInField()) {
				String realDescriptionfieldName = environmentSubstitute( meta.getRedmineDescriptionField() );
				data.indexOfDescriptionField = data.inputRowMeta.indexOfValue( ( realDescriptionfieldName ) );
				if ( data.indexOfDescriptionField < 0 ) {
		            // The field is unreachable !
					logError( BaseMessages.getString( PKG, "RedmineStep.Error.ErrorFindingField", realDescriptionfieldName ) );
		            throw new KettleException( BaseMessages.getString( PKG, "RedmineStep.Error.ErrorFindingField", realDescriptionfieldName ) );
				}
			}
			
			if (meta.isRedmineAssignedToInField()) {
				String realAssignedTofieldName = environmentSubstitute( meta.getRedmineAssignedToField() );
				data.indexOfAssignedToField = data.inputRowMeta.indexOfValue( ( realAssignedTofieldName ) );
				if ( data.indexOfAssignedToField < 0 ) {
		            // The field is unreachable !
					logError( BaseMessages.getString( PKG, "RedmineStep.Error.ErrorFindingField", realAssignedTofieldName ) );
		            throw new KettleException( BaseMessages.getString( PKG, "RedmineStep.Error.ErrorFindingField", realAssignedTofieldName ) );
				}
			}
		}

		try {
			// Create redmine client
			RedmineManager mgr = RedmineManagerFactory.createWithApiKey(meta.getRedmineUrl(), meta.getRedmineToken());

			Issue issue = new Issue();
			
			// project key
			ProjectManager projectManager = mgr.getProjectManager();
			issue.setProject(projectManager.getProjectByKey(meta.getRedmineProject()));
			
			if (meta.isRedmineSubjectInField()) {
				issue.setSubject(data.inputRowMeta.getString( r, data.indexOfSubjectField ));
			} else {
				issue.setSubject(meta.getRedmineSubject());
			}
			
			if (meta.isRedmineDescriptionInField()) {
				issue.setDescription(data.inputRowMeta.getString( r, data.indexOfDescriptionField ));
			} else {
				issue.setDescription(meta.getRedmineDescription());
			}
			
			if(meta.getRedmineCategory() != null) {
				try {
					issue.setCategory(IssueCategoryFactory.create(Integer.parseInt(meta.getRedmineCategory())));
				} catch (NumberFormatException e) {
					logError( BaseMessages.getString( PKG, "RedmineStep.Error.ErrorCategoryValue", meta.getRedmineCategory() ) );
					putError( getInputRowMeta(), r, 1, e.getMessage(), e.toString(), "-1" );
				}
			}
			
			// assign to
			if (meta.isRedmineAssignedToInField() && data.indexOfAssignedToField >= 0) {
				issue.setAssignee(UserFactory.create(data.inputRowMeta.getInteger(r, data.indexOfAssignedToField).intValue()));
			} else {
				if(meta.getRedmineAssigned() != null) {
					try {
						issue.setAssignee(UserFactory.create(Integer.parseInt(meta.getRedmineAssigned())));
					} catch (NumberFormatException e) {
						logError( BaseMessages.getString( PKG, "RedmineStep.Error.ErrorAssignedValue", meta.getRedmineAssigned() ) );
						putError( getInputRowMeta(), r, 1, e.getMessage(), e.toString(), "-1" );
					}
				}
			}
			
			if(isRowLevel()) {
				logRowlevel("issue project value: " + issue.getProject());
				logRowlevel("issue subject value: " + issue.getSubject());
				logRowlevel("issue description value: " + issue.getDescription());
				logRowlevel("issue category value: " + issue.getCategory());
				logRowlevel("issue assigned to value: " + issue.getAssignee());
			}
			
			// check allow duplications
			if (meta.isRedmineAllowDuplicates() || !isDuplicated(meta, mgr, issue)) {
				issue = mgr.getIssueManager().createIssue(issue);
				logBasic(BaseMessages.getString( PKG, "RedmineStep.Info.Success" ), issue.getId());
			} else {
				logBasic(BaseMessages.getString( PKG, "RedmineStep.Info.Skip" ));
			}
			
		} catch (RedmineException e) {
			logError( BaseMessages.getString( PKG, "RedmineStep.Error.Api" ), e );
			putError( getInputRowMeta(), r, 1, e.getMessage(), e.toString(), "-1" );
		}

		// indicate that processRow() should be called again
		return true;
	}

	/**
	 * This method is called by PDI once the step is done processing.
	 * 
	 * The dispose() method is the counterpart to init() and should release any
	 * resources acquired for step execution like file handles or database
	 * connections.
	 * 
	 * The meta and data implementations passed in can safely be cast to the step's
	 * respective implementations.
	 * 
	 * It is mandatory that super.dispose() is called to ensure correct behavior.
	 * 
	 * @param smi step meta interface implementation, containing the step settings
	 * @param sdi step data interface implementation, used to store runtime
	 *            information
	 */
	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {

		// Casting to step-specific implementation classes is safe
		RedmineStepMeta meta = (RedmineStepMeta) smi;
		RedmineStepData data = (RedmineStepData) sdi;

		// Call superclass dispose()
		super.dispose(meta, data);
	}
	
	private boolean isDuplicated(RedmineStepMeta meta, RedmineManager mgr, Issue issue) throws RedmineException {
		
		Map<String, String> parameters = new HashMap<String, String>();
		
		parameters.put("project_id", issue.getProject().getIdentifier());
		
		if (meta.isRedmineSearchFieldSubject()) {
			parameters.put("subject", issue.getSubject());
		}
		
		if (meta.isRedmineSearchFieldStatus()) {
			parameters.put("status_id", "1");
		}
		
		//List<Issue> result = mgr.getIssueManager().getIssuesBySummary(issue.getProject().getIdentifier(), issue.getSubject());
		List<Issue> result = mgr.getIssueManager().getIssues(parameters);
		
		return result != null && !result.isEmpty();
	}
}
