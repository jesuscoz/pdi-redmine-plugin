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

import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * This class is the implementation of StepMetaInterface. Classes implementing
 * this interface need to:
 * 
 * - keep track of the step settings - serialize step settings both to xml and a
 * repository - provide new instances of objects implementing
 * StepDialogInterface, StepInterface and StepDataInterface - report on how the
 * step modifies the meta-data of the row-stream (row structure and field types)
 * - perform a sanity-check on the settings provided by the user
 * 
 */

@Step(id = "Redmine-Plugin", 
      name = "RedmineStep.Name", 
      description = "RedmineStep.TooltipDesc",
      image = "es/jcozar/pdi/redmine/plugin/resources/logo.svg", 
      categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Output", 
      i18nPackageName = "es.jcozar.pdi.redmine.plugin")
@InjectionSupported(localizationPrefix = "RedmineStepMeta.Injection.")
public class RedmineStepMeta extends BaseStepMeta implements StepMetaInterface {

	/**
	 * The PKG member is used when looking up internationalized strings. The
	 * properties file with localized keys is expected to reside in {the package of
	 * the class specified}/messages/messages_{locale}.properties
	 */
	private static final Class<?> PKG = RedmineStepMeta.class; // for i18n purposes

	/**
	 * Stores the name of the field added to the row-stream.
	 */
	@Injection(name = "OUTPUT_FIELD")
	private String outputField;

	@Injection(name = "REDMINE_URL")
	private String redmineUrl;

	@Injection(name = "REDMINE_TOKEN")
	private String redmineToken;

	@Injection(name = "REDMINE_PROJECT")
	private String redmineProject;

	@Injection(name = "REDMINE_SUBJECT")
	private String redmineSubject;

	@Injection(name = "REDMINE_SUBJECT_IN_FIELD")
	private boolean redmineSubjectInField;

	@Injection(name = "REDMINE_SUBJECT_FIELD")
	private String redmineSubjectField;

	@Injection(name = "REDMINE_DESCRIPTION")
	private String redmineDescription;

	@Injection(name = "REDMINE_DESCRIPTION_IN_FIELD")
	private boolean redmineDescriptionInField;

	@Injection(name = "REDMINE_DESCRIPTION_FIELD")
	private String redmineDescriptionField;

	@Injection(name = "REDMINE_ASSIGNEDTO")
	private String redmineAssigned;
	
	@Injection(name = "REDMINE_ASSIGNED_TO_IN_FIELD")
	private boolean redmineAssignedToInField;

	@Injection(name = "REDMINE_ASSIGNED_TO_FIELD")
	private String redmineAssignedToField;

	@Injection(name = "REDMINE_CATEGORY")
	private String redmineCategory;

	/*
	 * ATTACH FILE 
	 */
	@Injection(name = "REDMINE_ATTACH")
	private boolean redmineAttachFile;

	@Injection(name = "REDMINE_ATTACH_FILE_NAME")
	private String redmineAttachFileName;

	@Injection(name = "REDMINE_ATTACH_FILE_CONTENT")
	private String redmineAttachFileContent;

	
	
	
	
	@Injection(name = "REDMINE_ALLOW_DUPLICATES")
	private boolean redmineAllowDuplicates;

	@Injection(name = "REDMINE_SEARCH_FIELD_SUBJECT")
	private boolean redmineSearchFieldSubject;
	
	@Injection(name = "REDMINE_SEARCH_FIELD_STATUS")
	private boolean redmineSearchFieldStatus;
	
	/**
	 * Constructor should call super() to make sure the base class has a chance to
	 * initialize properly.
	 */
	public RedmineStepMeta() {
		super();
	}

	/**
	 * Called by Spoon to get a new instance of the SWT dialog for the step. A
	 * standard implementation passing the arguments to the constructor of the step
	 * dialog is recommended.
	 * 
	 * @param shell     an SWT Shell
	 * @param meta      description of the step
	 * @param transMeta description of the the transformation
	 * @param name      the name of the step
	 * @return new instance of a dialog for this step
	 */
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface meta, TransMeta transMeta, String name) {
		return new RedmineStepDialog(shell, meta, transMeta, name);
	}

	/**
	 * Called by PDI to get a new instance of the step implementation. A standard
	 * implementation passing the arguments to the constructor of the step class is
	 * recommended.
	 * 
	 * @param stepMeta          description of the step
	 * @param stepDataInterface instance of a step data class
	 * @param cnr               copy number
	 * @param transMeta         description of the transformation
	 * @param disp              runtime implementation of the transformation
	 * @return the new instance of a step implementation
	 */
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
			Trans disp) {
		return new RedmineStep(stepMeta, stepDataInterface, cnr, transMeta, disp);
	}

	/**
	 * Called by PDI to get a new instance of the step data class.
	 */
	public StepDataInterface getStepData() {
		return new RedmineStepData();
	}

	/**
	 * This method is called every time a new step is created and should
	 * allocate/set the step configuration to sensible defaults. The values set here
	 * will be used by Spoon when a new step is created.
	 */
	public void setDefault() {
		setOutputField("demo_field");
		setRedmineUrl("http://localhost:8080/Redmine/api");
		setRedmineToken("");
		setRedmineProject("");
		setRedmineSubject("");
		setRedmineDescription("");
		setRedmineSubjectField("");
		setRedmineSubjectInField(false);
		setRedmineDescriptionField("");
		setRedmineDescriptionInField(false);
		setRedmineCategory("");
		setRedmineAssigned("");
		setRedmineAssignedToField("");
		setRedmineAssignedToInField(false);
		setRedmineAllowDuplicates(false);
		setRedmineSearchFieldSubject(true);
		setRedmineSearchFieldStatus(true);
		setRedmineAttachFile(false);
		setRedmineAttachFileContent("");
		setRedmineAttachFileName("");
	}
	
	@Override
	public boolean supportsErrorHandling() {
	    return true;
    }

	/**
	 * Getter for the name of the field added by this step
	 * 
	 * @return the name of the field added
	 */
	public String getOutputField() {
		return outputField;
	}

	/**
	 * Setter for the name of the field added by this step
	 * 
	 * @param outputField the name of the field added
	 */
	public void setOutputField(String outputField) {
		this.outputField = outputField;
	}

	public String getRedmineUrl() {
		return redmineUrl;
	}

	public void setRedmineUrl(String redmineUrl) {
		this.redmineUrl = redmineUrl;
	}

	public String getRedmineToken() {
		return redmineToken;
	}

	public void setRedmineToken(String redmineToken) {
		this.redmineToken = redmineToken;
	}

	public String getRedmineSubject() {
		return redmineSubject;
	}

	public void setRedmineSubject(String redmineSubject) {
		this.redmineSubject = redmineSubject;
	}

	public String getRedmineDescription() {
		return redmineDescription;
	}

	public void setRedmineDescription(String redmineDescription) {
		this.redmineDescription = redmineDescription;
	}

	public String getRedmineAssigned() {
		return redmineAssigned;
	}

	public void setRedmineAssigned(String redmineAssigned) {
		this.redmineAssigned = redmineAssigned;
	}

	public boolean isRedmineAssignedToInField() {
		return redmineAssignedToInField;
	}

	public void setRedmineAssignedToInField(boolean redmineAssignedToInField) {
		this.redmineAssignedToInField = redmineAssignedToInField;
	}

	public String getRedmineAssignedToField() {
		return redmineAssignedToField;
	}

	public void setRedmineAssignedToField(String redmineAssignedToField) {
		this.redmineAssignedToField = redmineAssignedToField;
	}

	public String getRedmineCategory() {
		return redmineCategory;
	}

	public void setRedmineCategory(String redmineCategory) {
		this.redmineCategory = redmineCategory;
	}

	public String getRedmineProject() {
		return redmineProject;
	}

	public void setRedmineProject(String redmineProject) {
		this.redmineProject = redmineProject;
	}

	public boolean isRedmineSubjectInField() {
		return redmineSubjectInField;
	}

	public void setRedmineSubjectInField(boolean redmineSubjectInField) {
		this.redmineSubjectInField = redmineSubjectInField;
	}

	public String getRedmineSubjectField() {
		return redmineSubjectField;
	}

	public void setRedmineSubjectField(String redmineSubjectField) {
		this.redmineSubjectField = redmineSubjectField;
	}

	public boolean isRedmineDescriptionInField() {
		return redmineDescriptionInField;
	}

	public void setRedmineDescriptionInField(boolean redmineDescriptionInField) {
		this.redmineDescriptionInField = redmineDescriptionInField;
	}

	public String getRedmineDescriptionField() {
		return redmineDescriptionField;
	}

	public void setRedmineDescriptionField(String redmineDescriptionField) {
		this.redmineDescriptionField = redmineDescriptionField;
	}

	public boolean isRedmineAllowDuplicates() {
		return redmineAllowDuplicates;
	}

	public void setRedmineAllowDuplicates(boolean redmineAllowDuplicates) {
		this.redmineAllowDuplicates = redmineAllowDuplicates;
	}

	public boolean isRedmineSearchFieldSubject() {
		return redmineSearchFieldSubject;
	}

	public void setRedmineSearchFieldSubject(boolean redmineSearchFieldSubject) {
		this.redmineSearchFieldSubject = redmineSearchFieldSubject;
	}

	public boolean isRedmineSearchFieldStatus() {
		return redmineSearchFieldStatus;
	}

	public void setRedmineSearchFieldStatus(boolean redmineSearchFieldStatus) {
		this.redmineSearchFieldStatus = redmineSearchFieldStatus;
	}

	public boolean isRedmineAttachFile() {
		return redmineAttachFile;
	}

	public void setRedmineAttachFile(boolean redmineAttachFile) {
		this.redmineAttachFile = redmineAttachFile;
	}

	public String getRedmineAttachFileName() {
		return redmineAttachFileName;
	}

	public void setRedmineAttachFileName(String redmineAttachFileName) {
		this.redmineAttachFileName = redmineAttachFileName;
	}

	public String getRedmineAttachFileContent() {
		return redmineAttachFileContent;
	}

	public void setRedmineAttachFileContent(String redmineAttachFileContent) {
		this.redmineAttachFileContent = redmineAttachFileContent;
	}

	/**
	 * This method is used when a step is duplicated in Spoon. It needs to return a
	 * deep copy of this step meta object. Be sure to create proper deep copies if
	 * the step configuration is stored in modifiable objects.
	 * 
	 * See org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta.clone() for an
	 * example on creating a deep copy.
	 * 
	 * @return a deep copy of this
	 */
	public Object clone() {
		Object retval = super.clone();
		return retval;
	}

	/**
	 * This method is called by Spoon when a step needs to serialize its
	 * configuration to XML. The expected return value is an XML fragment consisting
	 * of one or more XML tags.
	 * 
	 * Please use org.pentaho.di.core.xml.XMLHandler to conveniently generate the
	 * XML.
	 * 
	 * @return a string containing the XML serialization of this step
	 */
	public String getXML() throws KettleValueException {
		StringBuilder xml = new StringBuilder();

		// only one field to serialize
		xml.append(XMLHandler.addTagValue("outputfield", outputField));
		xml.append(XMLHandler.addTagValue("redmineUrl", redmineUrl));
		xml.append(XMLHandler.addTagValue("redmineToken", redmineToken));
		xml.append(XMLHandler.addTagValue("redmineSubject", redmineSubject));
		xml.append(XMLHandler.addTagValue("redmineSubjectField", redmineSubjectField));
		xml.append(XMLHandler.addTagValue("redmineSubjectInField", redmineSubjectInField));
		xml.append(XMLHandler.addTagValue("redmineDescription", redmineDescription));
		xml.append(XMLHandler.addTagValue("redmineDescriptionField", redmineDescriptionField));
		xml.append(XMLHandler.addTagValue("redmineDescriptionInField", redmineDescriptionInField));
		xml.append(XMLHandler.addTagValue("redmineAssigned", redmineAssigned));
		xml.append(XMLHandler.addTagValue("redmineAssignedToField", redmineAssignedToField));
		xml.append(XMLHandler.addTagValue("redmineAssignedToInField", redmineAssignedToInField));
		xml.append(XMLHandler.addTagValue("redmineCategory", redmineCategory));
		xml.append(XMLHandler.addTagValue("redmineProject", redmineProject));
		xml.append(XMLHandler.addTagValue("redmineAllowDuplicates", redmineAllowDuplicates));
		xml.append(XMLHandler.addTagValue("redmineSearchFieldStatus", redmineSearchFieldStatus));
		xml.append(XMLHandler.addTagValue("redmineSearchFieldSubject", redmineSearchFieldSubject));
		xml.append(XMLHandler.addTagValue("redmineAttachFile", redmineAttachFile));
		xml.append(XMLHandler.addTagValue("redmineAttachFileName", redmineAttachFileName));
		xml.append(XMLHandler.addTagValue("redmineAttachFileContent", redmineAttachFileContent));
		
		return xml.toString();
	}

	/**
	 * This method is called by PDI when a step needs to load its configuration from
	 * XML.
	 * 
	 * Please use org.pentaho.di.core.xml.XMLHandler to conveniently read from the
	 * XML node passed in.
	 * 
	 * @param stepnode  the XML node containing the configuration
	 * @param databases the databases available in the transformation
	 * @param metaStore the metaStore to optionally read from
	 */
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore) throws KettleXMLException {
		try {
			setOutputField(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "outputfield")));
			setRedmineUrl(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "redmineUrl")));
			setRedmineToken(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "redmineToken")));
			setRedmineSubject(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "redmineSubject")));
			setRedmineSubjectField(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "redmineSubjectField")));
			setRedmineSubjectInField("Y".equalsIgnoreCase(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "redmineSubjectInField"))));
			setRedmineDescription(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "redmineDescription")));
			setRedmineDescriptionField(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "redmineDescriptionField")));
			setRedmineDescriptionInField("Y".equalsIgnoreCase(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "redmineDescriptionInField"))));
			setRedmineAssigned(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "redmineAssigned")));
			setRedmineAssignedToField(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "redmineAssignedToField")));
			setRedmineAssignedToInField("Y".equalsIgnoreCase(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "redmineAssignedToInField"))));
			setRedmineCategory(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "redmineCategory")));
			setRedmineProject(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "redmineProject")));
			setRedmineAllowDuplicates("Y".equalsIgnoreCase(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "redmineAllowDuplicates"))));
			setRedmineSearchFieldStatus("Y".equalsIgnoreCase(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "redmineSearchFieldStatus"))));
			setRedmineSearchFieldSubject("Y".equalsIgnoreCase(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "redmineSearchFieldSubject"))));
			setRedmineAttachFile("Y".equalsIgnoreCase(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "redmineAttachFile"))));
			setRedmineAttachFileName(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "redmineAttachFileName")));
			setRedmineAttachFileContent(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "redmineAttachFileContent")));
			
		} catch (Exception e) {
			throw new KettleXMLException("Redmine plugin unable to read step info from XML node", e);
		}
	}

	/**
	 * This method is called by Spoon when a step needs to serialize its
	 * configuration to a repository. The repository implementation provides the
	 * necessary methods to save the step attributes.
	 *
	 * @param rep               the repository to save to
	 * @param metaStore         the metaStore to optionally write to
	 * @param id_transformation the id to use for the transformation when saving
	 * @param id_step           the id to use for the step when saving
	 */
	public void saveRep(Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step)
			throws KettleException {
		try {
			rep.saveStepAttribute(id_transformation, id_step, "outputfield", outputField); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "redmineUrl", redmineUrl); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "redmineToken", redmineToken); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "redmineSubject", redmineSubject); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "redmineSubjectField", redmineSubjectField); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "redmineSubjectInField", redmineSubjectInField); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "redmineDescription", redmineDescription); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "redmineDescriptionField", redmineDescriptionField); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "redmineDescriptionInField", redmineDescriptionInField); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "redmineAssigned", redmineAssigned); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "redmineAssignedToField", redmineAssignedToField); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "redmineAssignedToInField", redmineAssignedToInField); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "redmineCategory", redmineCategory); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "redmineProject", redmineProject); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "redmineAllowDuplicates", redmineAllowDuplicates); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "redmineSearchFieldStatus", redmineSearchFieldStatus); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "redmineSearchFieldSubject", redmineSearchFieldSubject); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "redmineAttachFile", redmineAttachFile); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "redmineAttachFileName", redmineAttachFileName); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "redmineAttachFileContent", redmineAttachFileContent); //$NON-NLS-1$
			
		} catch (Exception e) {
			throw new KettleException("Unable to save step into repository: " + id_step, e);
		}
	}

	/**
	 * This method is called by PDI when a step needs to read its configuration from
	 * a repository. The repository implementation provides the necessary methods to
	 * read the step attributes.
	 * 
	 * @param rep       the repository to read from
	 * @param metaStore the metaStore to optionally read from
	 * @param id_step   the id of the step being read
	 * @param databases the databases available in the transformation
	 */
	public void readRep(Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases) throws KettleException {
		try {
			outputField = rep.getStepAttributeString(id_step, "outputfield"); //$NON-NLS-1$
			redmineUrl = rep.getStepAttributeString(id_step, "redmineUrl"); //$NON-NLS-1$
			redmineToken = rep.getStepAttributeString(id_step, "redmineToken"); //$NON-NLS-1$
			redmineSubject = rep.getStepAttributeString(id_step, "redmineSubject"); //$NON-NLS-1$
			redmineSubjectField = rep.getStepAttributeString(id_step, "redmineSubjectField"); //$NON-NLS-1$
			redmineSubjectInField = rep.getStepAttributeBoolean(id_step, "redmineSubjectInField"); //$NON-NLS-1$
			redmineDescription = rep.getStepAttributeString(id_step, "redmineDescription"); //$NON-NLS-1$
			redmineDescriptionField = rep.getStepAttributeString(id_step, "redmineDescriptionField"); //$NON-NLS-1$
			redmineDescriptionInField = rep.getStepAttributeBoolean(id_step, "redmineDescriptionInField"); //$NON-NLS-1$
			redmineAssigned = rep.getStepAttributeString(id_step, "redmineAssigned"); //$NON-NLS-1$
			redmineAssignedToField = rep.getStepAttributeString(id_step, "redmineAssignedToField"); //$NON-NLS-1$
			redmineAssignedToInField = rep.getStepAttributeBoolean(id_step, "redmineAssignedToInField"); //$NON-NLS-1$
			redmineCategory = rep.getStepAttributeString(id_step, "redmineCategory"); //$NON-NLS-1$
			redmineProject = rep.getStepAttributeString(id_step, "redmineProject"); //$NON-NLS-1$
			redmineAllowDuplicates = rep.getStepAttributeBoolean(id_step, "redmineAllowDuplicates"); //$NON-NLS-1$
			redmineSearchFieldStatus = rep.getStepAttributeBoolean(id_step, "redmineSearchFieldStatus"); //$NON-NLS-1$
			redmineSearchFieldSubject = rep.getStepAttributeBoolean(id_step, "redmineSearchFieldSubject"); //$NON-NLS-1$
			redmineAttachFile = rep.getStepAttributeBoolean(id_step, "redmineAttachFile"); //$NON-NLS-1$
			redmineAttachFileName = rep.getStepAttributeString(id_step, "redmineAttachFileName"); //$NON-NLS-1$
			redmineAttachFileContent = rep.getStepAttributeString(id_step, "redmineAttachFileContent"); //$NON-NLS-1$
			
		} catch (Exception e) {
			throw new KettleException("Unable to load step from repository", e);
		}
	}

	/**
	 * This method is called to determine the changes the step is making to the
	 * row-stream. To that end a RowMetaInterface object is passed in, containing
	 * the row-stream structure as it is when entering the step. This method must
	 * apply any changes the step makes to the row stream. Usually a step adds
	 * fields to the row-stream.
	 * 
	 * @param inputRowMeta the row structure coming in to the step
	 * @param name         the name of the step making the changes
	 * @param info         row structures of any info steps coming in
	 * @param nextStep     the description of a step this step is passing rows to
	 * @param space        the variable space for resolving variables
	 * @param repository   the repository instance optionally read from
	 * @param metaStore    the metaStore to optionally read from
	 */
	public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep,
			VariableSpace space, Repository repository, IMetaStore metaStore) throws KettleStepException {

		// do nothing
	}

	/**
	 * This method is called when the user selects the "Verify Transformation"
	 * option in Spoon. A list of remarks is passed in that this method should add
	 * to. Each remark is a comment, warning, error, or ok. The method should
	 * perform as many checks as necessary to catch design-time errors.
	 * 
	 * Typical checks include: - verify that all mandatory configuration is given -
	 * verify that the step receives any input, unless it's a row generating step -
	 * verify that the step does not receive any input if it does not take them into
	 * account - verify that the step finds fields it relies on in the row-stream
	 * 
	 * @param remarks   the list of remarks to append to
	 * @param transMeta the description of the transformation
	 * @param stepMeta  the description of the step
	 * @param prev      the structure of the incoming row-stream
	 * @param input     names of steps sending input to the step
	 * @param output    names of steps this step is sending output to
	 * @param info      fields coming in from info steps
	 * @param metaStore metaStore to optionally read from
	 */
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
			String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
			IMetaStore metaStore) {
		CheckResult cr;

		// See if there are input streams leading to this step!
		if (input != null && input.length > 0) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK,
					BaseMessages.getString(PKG, "Redmine.CheckResult.ReceivingRows.OK"), stepMeta);
			remarks.add(cr);
		} else {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
					BaseMessages.getString(PKG, "Redmine.CheckResult.ReceivingRows.ERROR"), stepMeta);
			remarks.add(cr);
		}
	}
}
