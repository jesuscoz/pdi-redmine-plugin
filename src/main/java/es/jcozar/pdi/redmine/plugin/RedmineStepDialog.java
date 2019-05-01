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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.LabelText;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;

/**
 * This class is part of the demo step plug-in implementation.
 * It demonstrates the basics of developing a plug-in step for PDI.  
 *  
 * The demo step adds a new string field to the row stream and sets its
 * value to "Hello World!". The user may select the name of the new field.
 *  
 * This class is the implementation of StepDialogInterface.
 * Classes implementing this interface need to:
 *  
 * - build and open a SWT dialog displaying the step's settings (stored in the step's meta object)
 * - write back any changes the user makes to the step's meta object
 * - report whether the user changed any settings when confirming the dialog  
 *  
 */
public class RedmineStepDialog extends BaseStepDialog implements StepDialogInterface {

  /**
   *  The PKG member is used when looking up internationalized strings.
   *  The properties file with localized keys is expected to reside in  
   *  {the package of the class specified}/messages/messages_{locale}.properties  
   */
  private static Class<?> PKG = RedmineStepMeta.class; // for i18n purposes  

  // this is the object the stores the step's settings
  // the dialog reads the settings from it when opening
  // the dialog writes the settings to it when confirmed  
  private RedmineStepMeta meta;
  private Map<String, Integer> inputFields;
  private boolean gotPreviousFields;

  private Label wlSubjectInField, wlSubjectField, wlDescriptionInField, wlDescriptionField, wlAssignedToInField, wlAssignedToField, wlAllowDuplications;
  private Button  wSubjectInField,wDescriptionInField,wAssignedToInField,wAllowDuplications;
  private ComboVar wSubjectField,wDescriptionField,wAssignedToField;
  
  private LabelText wRedmineURL;
  private LabelText wRedmineToken;
  private LabelText wRedmineProject;
  private LabelText wRedmineCategory;
  private LabelText wRedmineAssignedTo;
  private LabelText wRedmineSubject;
  private LabelText wRedmineDescription;

  /**
   * The constructor should simply invoke super() and save the incoming meta
   * object to a local variable, so it can conveniently read and write settings
   * from/to it.
   *  
   * @param parent   the SWT shell to open the dialog in
   * @param in    the meta object holding the step's settings
   * @param transMeta  transformation description
   * @param sname    the step name
   */
  public RedmineStepDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, (BaseStepMeta) in, transMeta, sname );
    meta = (RedmineStepMeta) in;
    inputFields = new HashMap<String, Integer>();
  }

  /**
   * This method is called by Spoon when the user opens the settings dialog of the step.
   * It should open the dialog and return only once the dialog has been closed by the user.
   *  
   * If the user confirms the dialog, the meta object (passed in the constructor) must
   * be updated to reflect the new step settings. The changed flag of the meta object must  
   * reflect whether the step configuration was changed by the dialog.
   *  
   * If the user cancels the dialog, the meta object must not be updated, and its changed flag
   * must remain unaltered.
   *  
   * The open() method must return the name of the step after the user has confirmed the dialog,
   * or null if the user cancelled the dialog.
   */
  public String open() {
    // store some convenient SWT variables  
    Shell parent = getParent();
    Display display = parent.getDisplay();

    // SWT code for preparing the dialog
    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX );
    props.setLook( shell );
    setShellImage( shell, meta );

    // Save the value of the changed flag on the meta object. If the user cancels
    // the dialog, it will be restored to this saved value.
    // The "changed" variable is inherited from BaseStepDialog
    changed = meta.hasChanged();

    // The ModifyListener used on all controls. It will update the meta object to  
    // indicate that changes are being made.
    ModifyListener lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        meta.setChanged();
      }
    };

    
    
    // ------------------------------------------------------- //
    // SWT code for building the actual settings dialog        //
    // ------------------------------------------------------- //
    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;
    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "Redmine.Shell.Title" ) );
    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;



    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "System.Label.StepName" ) );
    props.setLook( wlStepname );
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment( 0, 0 );
    fdlStepname.right = new FormAttachment( middle, -margin );
    fdlStepname.top = new FormAttachment( 0, margin );
    wlStepname.setLayoutData( fdlStepname );

    wStepname = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wStepname.setText( stepname );
    props.setLook( wStepname );
    wStepname.addModifyListener( lsMod );
    fdStepname = new FormData();
    fdStepname.left = new FormAttachment( middle, 0 );
    fdStepname.top = new FormAttachment( 0, margin );
    fdStepname.right = new FormAttachment( 100, 0 );
    wStepname.setLayoutData( fdStepname );
    
    
    // ------------------------- //
    // settings group            //
    // ------------------------- //
    
    Group gSettings = new Group( shell, SWT.SHADOW_ETCHED_IN );
    gSettings.setText( BaseMessages.getString( PKG, "Redmine.SettingsGroup.Label" ) );
    FormLayout SettingsLayout = new FormLayout();
    SettingsLayout.marginWidth = 3;
    SettingsLayout.marginHeight = 3;
    gSettings.setLayout( SettingsLayout );
    props.setLook( gSettings );
    FormData fdSettings = new FormData();
    fdSettings.left = new FormAttachment( 0, 0 );
    fdSettings.right = new FormAttachment( 100, 0 );
    fdSettings.top = new FormAttachment( wStepname, margin );
    gSettings.setLayoutData( fdSettings );

    // URL
    wRedmineURL = new LabelText( gSettings, BaseMessages.getString( PKG, "Redmine.URL.Label" ), null );
    props.setLook( wRedmineURL );
    wRedmineURL.addModifyListener( lsMod );
    FormData fdRedmineUrl = new FormData();
    fdRedmineUrl.left = new FormAttachment( 0, 0 );
    fdRedmineUrl.right = new FormAttachment( 100, 0 );
    fdRedmineUrl.top = new FormAttachment( wStepname, margin );
    wRedmineURL.setLayoutData( fdRedmineUrl );
    
    // TOKEN
    wRedmineToken = new LabelText( gSettings, BaseMessages.getString( PKG, "Redmine.Token.Label" ), null );
    props.setLook( wRedmineToken );
    wRedmineToken.addModifyListener( lsMod );
    FormData fdRedmineToken = new FormData();
    fdRedmineToken.left = new FormAttachment( 0, 0 );
    fdRedmineToken.right = new FormAttachment( 100, 0 );
    fdRedmineToken.top = new FormAttachment( wRedmineURL, margin );
    wRedmineToken.setLayoutData( fdRedmineToken );

    // Project
    wRedmineProject = new LabelText( gSettings, BaseMessages.getString( PKG, "Redmine.Project.Label" ), null );
    props.setLook( wRedmineProject );
    wRedmineProject.addModifyListener( lsMod );
    FormData fdRedmineProject= new FormData();
    fdRedmineProject.left = new FormAttachment( 0, 0 );
    fdRedmineProject.right = new FormAttachment( 100, 0 );
    fdRedmineProject.top = new FormAttachment( wRedmineToken, margin );
    wRedmineProject.setLayoutData( fdRedmineProject );

    

    // ------------------------- //
    // Issues group              //
    // ------------------------- //
    
    Group gIssues = new Group( shell, SWT.SHADOW_ETCHED_IN );
    gIssues.setText( BaseMessages.getString( PKG, "Redmine.IssuesGroup.Label" ) );
    FormLayout IssuesLayout = new FormLayout();
    IssuesLayout.marginWidth = 3;
    IssuesLayout.marginHeight = 3;
    gIssues.setLayout( IssuesLayout );
    props.setLook( gIssues );
    FormData fdOutputFields = new FormData();
    fdOutputFields.left = new FormAttachment( 0, 0 );
    fdOutputFields.right = new FormAttachment( 100, 0 );
    fdOutputFields.top = new FormAttachment( gSettings, margin );
    gIssues.setLayoutData( fdOutputFields );
    
    
    // Subject
    wRedmineSubject = new LabelText( gIssues, BaseMessages.getString( PKG, "Redmine.Subject.Label" ), null );
    props.setLook( wRedmineSubject );
    wRedmineSubject.addModifyListener( lsMod );
    FormData fdRedmineSubject = new FormData();
    fdRedmineSubject.left = new FormAttachment( 0, 0 );
    fdRedmineSubject.right = new FormAttachment( 100, 0 );
    fdRedmineSubject.top = new FormAttachment( wRedmineProject, margin );
    wRedmineSubject.setLayoutData( fdRedmineSubject );
    
    // subject from field check
    wlSubjectInField = new Label( gIssues, SWT.RIGHT );
    wlSubjectInField.setText( BaseMessages.getString( PKG, "Redmine.SubjectInField.Label" ) );
    props.setLook( wlSubjectInField );
    FormData fdlSubjectInField = new FormData();
    fdlSubjectInField.left = new FormAttachment( 0, 0 );
    fdlSubjectInField.top = new FormAttachment( wRedmineSubject, margin );
    fdlSubjectInField.right = new FormAttachment( middle, -margin );
    wlSubjectInField.setLayoutData( fdlSubjectInField );
    wSubjectInField = new Button( gIssues, SWT.CHECK );
    props.setLook( wSubjectInField );
    FormData fdSubjectInField = new FormData();
    fdSubjectInField.left = new FormAttachment( middle, 0 );
    fdSubjectInField.top = new FormAttachment( wRedmineSubject, margin );
    fdSubjectInField.right = new FormAttachment( 100, 0 );
    wSubjectInField.setLayoutData( fdSubjectInField );
    wSubjectInField.addSelectionListener( new SelectionAdapter() {
	      public void widgetSelected( SelectionEvent e ) {
	        meta.setChanged();
	        activeSubjectInfield();
	      }
	} );
    
    // subject from field
    wlSubjectField = new Label( gIssues, SWT.RIGHT );
    wlSubjectField.setText( BaseMessages.getString( PKG, "Redmine.SubjectField.Label" ) );
    props.setLook( wlSubjectField );
    FormData fdlUrlField = new FormData();
    fdlUrlField.left = new FormAttachment( 0, 0 );
    fdlUrlField.right = new FormAttachment( middle, -margin );
    fdlUrlField.top = new FormAttachment( wSubjectInField, margin );
    wlSubjectField.setLayoutData( fdlUrlField );

    wSubjectField = new ComboVar( transMeta, gIssues, SWT.BORDER | SWT.READ_ONLY );
    wSubjectField.setEditable( true );
    props.setLook( wSubjectField );
    wSubjectField.addModifyListener( lsMod );
    FormData fdUrlField = new FormData();
    fdUrlField.left = new FormAttachment( middle, 0 );
    fdUrlField.top = new FormAttachment( wSubjectInField, margin );
    fdUrlField.right = new FormAttachment( 100, -margin );
    wSubjectField.setLayoutData( fdUrlField );
    wSubjectField.addFocusListener( new FocusListener() {
    	public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
    	}

    	public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
    		Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
    		shell.setCursor( busy );
        	setStreamFields();
        	shell.setCursor( null );
        	busy.dispose();
    	}
    } );

    // Description
    wRedmineDescription = new LabelText( gIssues, BaseMessages.getString( PKG, "Redmine.Description.Label" ), null );
    props.setLook( wRedmineDescription );
    wRedmineDescription.addModifyListener( lsMod );
    FormData fdRedmineDescription = new FormData();
    fdRedmineDescription.left = new FormAttachment( 0, 0 );
    fdRedmineDescription.right = new FormAttachment( 100, 0 );
    fdRedmineDescription.top = new FormAttachment( wSubjectField, margin );
    wRedmineDescription.setLayoutData( fdRedmineDescription );

    
    // description from field check
    wlDescriptionInField = new Label( gIssues, SWT.RIGHT );
    wlDescriptionInField.setText( BaseMessages.getString( PKG, "Redmine.DescriptionInField.Label" ) );
    props.setLook( wlDescriptionInField );
    FormData fdlDescriptionInField = new FormData();
    fdlDescriptionInField.left = new FormAttachment( 0, 0 );
    fdlDescriptionInField.top = new FormAttachment( wRedmineDescription, margin );
    fdlDescriptionInField.right = new FormAttachment( middle, -margin );
    wlDescriptionInField.setLayoutData( fdlDescriptionInField );
    wDescriptionInField = new Button( gIssues, SWT.CHECK );
    props.setLook( wDescriptionInField );
    FormData fdDescriptionInField = new FormData();
    fdDescriptionInField.left = new FormAttachment( middle, 0 );
    fdDescriptionInField.top = new FormAttachment( wRedmineDescription, margin );
    fdDescriptionInField.right = new FormAttachment( 100, 0 );
    wDescriptionInField.setLayoutData( fdDescriptionInField );
    wDescriptionInField.addSelectionListener( new SelectionAdapter() {
	      public void widgetSelected( SelectionEvent e ) {
	        meta.setChanged();
	        activeDescriptionInfield();
	      }
	} );
    
    // description from field
    wlDescriptionField = new Label( gIssues, SWT.RIGHT );
    wlDescriptionField.setText( BaseMessages.getString( PKG, "Redmine.DescriptionField.Label" ) );
    props.setLook( wlDescriptionField );
    FormData fdwlDescriptionField = new FormData();
    fdwlDescriptionField.left = new FormAttachment( 0, 0 );
    fdwlDescriptionField.right = new FormAttachment( middle, -margin );
    fdwlDescriptionField.top = new FormAttachment( wDescriptionInField, margin );
    wlDescriptionField.setLayoutData( fdwlDescriptionField );

    wDescriptionField = new ComboVar( transMeta, gIssues, SWT.BORDER | SWT.READ_ONLY );
    wDescriptionField.setEditable( true );
    props.setLook( wDescriptionField );
    wDescriptionField.addModifyListener( lsMod );
    FormData fdwDescriptionField = new FormData();
    fdwDescriptionField.left = new FormAttachment( middle, 0 );
    fdwDescriptionField.top = new FormAttachment( wDescriptionInField, margin );
    fdwDescriptionField.right = new FormAttachment( 100, -margin );
    wDescriptionField.setLayoutData( fdwDescriptionField );
    wDescriptionField.addFocusListener( new FocusListener() {
    	public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
    	}

    	public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
    		Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
    		shell.setCursor( busy );
        	setStreamFields();
        	shell.setCursor( null );
        	busy.dispose();
    	}
    } );
    
    // CATEGORY
    wRedmineCategory = new LabelText( gIssues, BaseMessages.getString( PKG, "Redmine.Category.Label" ), null );
    props.setLook( wRedmineCategory );
    wRedmineCategory.addModifyListener( lsMod );
    FormData fdRedmineCategory = new FormData();
    fdRedmineCategory.left = new FormAttachment( 0, 0 );
    fdRedmineCategory.right = new FormAttachment( 100, 0 );
    fdRedmineCategory.top = new FormAttachment( wDescriptionField, margin );
    wRedmineCategory.setLayoutData( fdRedmineCategory );
    
    // ASSIGNED TO
    wRedmineAssignedTo = new LabelText( gIssues, BaseMessages.getString( PKG, "Redmine.AssignedTo.Label" ), null );
    props.setLook( wRedmineAssignedTo );
    wRedmineAssignedTo.addModifyListener( lsMod );
    FormData fdRedmineAssignedTo = new FormData();
    fdRedmineAssignedTo.left = new FormAttachment( 0, 0 );
    fdRedmineAssignedTo.right = new FormAttachment( 100, 0 );
    fdRedmineAssignedTo.top = new FormAttachment( wRedmineCategory, margin );
    wRedmineAssignedTo.setLayoutData( fdRedmineAssignedTo );
    
    // assigned to from field check
    wlAssignedToInField = new Label( gIssues, SWT.RIGHT );
    wlAssignedToInField.setText( BaseMessages.getString( PKG, "Redmine.AssignedToInField.Label" ) );
    props.setLook( wlAssignedToInField );
    FormData fdlAssignedToInField = new FormData();
    fdlAssignedToInField.left = new FormAttachment( 0, 0 );
    fdlAssignedToInField.top = new FormAttachment( wRedmineAssignedTo, margin );
    fdlAssignedToInField.right = new FormAttachment( middle, -margin );
    wlAssignedToInField.setLayoutData( fdlAssignedToInField );
    wAssignedToInField = new Button( gIssues, SWT.CHECK );
    props.setLook( wAssignedToInField );
    FormData fdAssignedToInField = new FormData();
    fdAssignedToInField.left = new FormAttachment( middle, 0 );
    fdAssignedToInField.top = new FormAttachment( wRedmineAssignedTo, margin );
    fdAssignedToInField.right = new FormAttachment( 100, 0 );
    wAssignedToInField.setLayoutData( fdAssignedToInField );
    wAssignedToInField.addSelectionListener( new SelectionAdapter() {
	      public void widgetSelected( SelectionEvent e ) {
	        meta.setChanged();
	        activeAssignedToInfield();
	      }
	} );
    
    // assigned to from field
    wlAssignedToField = new Label( gIssues, SWT.RIGHT );
    wlAssignedToField.setText( BaseMessages.getString( PKG, "Redmine.AssignedToField.Label" ) );
    props.setLook( wlAssignedToField );
    FormData fdwlAssignedToField = new FormData();
    fdwlAssignedToField.left = new FormAttachment( 0, 0 );
    fdwlAssignedToField.right = new FormAttachment( middle, -margin );
    fdwlAssignedToField.top = new FormAttachment( wAssignedToInField, margin );
    wlAssignedToField.setLayoutData( fdwlAssignedToField );

    wAssignedToField = new ComboVar( transMeta, gIssues, SWT.BORDER | SWT.READ_ONLY );
    wAssignedToField.setEditable( true );
    props.setLook( wAssignedToField );
    wAssignedToField.addModifyListener( lsMod );
    FormData fdwAssignedToField = new FormData();
    fdwAssignedToField.left = new FormAttachment( middle, 0 );
    fdwAssignedToField.top = new FormAttachment( wAssignedToInField, margin );
    fdwAssignedToField.right = new FormAttachment( 100, -margin );
    wAssignedToField.setLayoutData( fdwAssignedToField );
    wAssignedToField.addFocusListener( new FocusListener() {
    	public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
    	}

    	public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
    		Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
    		shell.setCursor( busy );
        	setStreamFields();
        	shell.setCursor( null );
        	busy.dispose();
    	}
    } );
    
    

    // ------------------------- //
    // Search group              //
    // ------------------------- //
    
    Group gSearch = new Group( shell, SWT.SHADOW_ETCHED_IN );
    gSearch.setText( BaseMessages.getString( PKG, "Redmine.SearchGroup.Label" ) );
    FormLayout SearchLayout = new FormLayout();
    SearchLayout.marginWidth = 3;
    SearchLayout.marginHeight = 3;
    gSearch.setLayout( SearchLayout );
    props.setLook( gSearch );
    FormData fdSearchFields = new FormData();
    fdSearchFields.left = new FormAttachment( 0, 0 );
    fdSearchFields.right = new FormAttachment( 100, 0 );
    fdSearchFields.top = new FormAttachment( gIssues, margin );
    gSearch.setLayoutData( fdSearchFields );
    
    
    // allow duplications check
    wlAllowDuplications = new Label( gSearch, SWT.RIGHT );
    wlAllowDuplications.setText( BaseMessages.getString( PKG, "Redmine.AllowDuplications.Label" ) );
    props.setLook( wlAllowDuplications );
    FormData fdlAllowDuplications = new FormData();
    fdlAllowDuplications.left = new FormAttachment( 0, 0 );
    fdlAllowDuplications.top = new FormAttachment( gSearch, margin );
    fdlAllowDuplications.right = new FormAttachment( middle, -margin );
    wlAllowDuplications.setLayoutData( fdlAllowDuplications );
    wAllowDuplications = new Button( gSearch, SWT.CHECK );
    props.setLook( wAllowDuplications );
    FormData fdAllowDuplications = new FormData();
    fdAllowDuplications.left = new FormAttachment( middle, 0 );
    fdAllowDuplications.top = new FormAttachment( gSearch, margin );
    fdAllowDuplications.right = new FormAttachment( 100, 0 );
    wAllowDuplications.setLayoutData( fdAllowDuplications );
    
    
    //
    // Search the fields in the background
    //
    final Runnable runnable = new Runnable() {
      public void run() {
        StepMeta stepMeta = transMeta.findStep( stepname );
        if ( stepMeta != null ) {
          try {
            RowMetaInterface row = transMeta.getPrevStepFields( stepMeta );

            // Remember these fields...
            for ( int i = 0; i < row.size(); i++ ) {
              inputFields.put( row.getValueMeta( i ).getName(), Integer.valueOf( i ) );
            }

            //setComboBoxes();
          } catch ( KettleException e ) {
            logError( BaseMessages.getString( PKG, "System.Dialog.GetFieldsFailed.Message" ) );
          }
        }
      }
    };
    new Thread( runnable ).start();
    

    // OK and cancel buttons
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );
    setButtonPositions( new Button[] { wOK, wCancel }, margin, gSearch );

    // Add listeners for cancel and OK
    lsCancel = new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    };
    lsOK = new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    };
    wCancel.addListener( SWT.Selection, lsCancel );
    wOK.addListener( SWT.Selection, lsOK );

    // default listener (for hitting "enter")
    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };
    wStepname.addSelectionListener( lsDef );
    wRedmineURL.addSelectionListener( lsDef );
    wRedmineToken.addSelectionListener( lsDef );
    wRedmineSubject.addSelectionListener( lsDef );
    wSubjectField.addSelectionListener( lsDef );
    wSubjectInField.addSelectionListener( lsDef );
    wRedmineDescription.addSelectionListener( lsDef );
    wDescriptionField.addSelectionListener( lsDef );
    wDescriptionInField.addSelectionListener( lsDef );
    wRedmineAssignedTo.addSelectionListener( lsDef );
    wAssignedToField.addSelectionListener( lsDef );
    wAssignedToInField.addSelectionListener( lsDef );
    wRedmineCategory.addSelectionListener( lsDef );
    wRedmineProject.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window and cancel the dialog properly
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    // Set/Restore the dialog size based on last position on screen
    // The setSize() method is inherited from BaseStepDialog
    setSize(shell, 400, 350);

    // populate the dialog with the values from the meta object
    populateDialog();

    // restore the changed flag to original value, as the modify listeners fire during dialog population  
    meta.setChanged( changed );

    // open dialog and enter event loop  
    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }

    // at this point the dialog has closed, so either ok() or cancel() have been executed
    // The "stepname" variable is inherited from BaseStepDialog
    return stepname;
  }

  /**
   * This helper method puts the step configuration stored in the meta object
   * and puts it into the dialog controls.
   */
  private void populateDialog() {
    wStepname.selectAll();
    
    if ( meta.getRedmineUrl() != null ) {
    	wRedmineURL.setText(meta.getRedmineUrl());
    }
    
    if ( meta.getRedmineToken() != null ) {
    	wRedmineToken.setText(meta.getRedmineToken());
    }
    
    if ( meta.getRedmineProject() != null ) {
    	wRedmineProject.setText(meta.getRedmineProject());
    }
    
    if ( meta.getRedmineCategory() != null ) {
    	wRedmineCategory.setText(meta.getRedmineCategory());
    }
    
    if ( meta.getRedmineAssigned() != null ) {
    	wRedmineAssignedTo.setText(meta.getRedmineAssigned());
    }
    
    if ( meta.getRedmineAssignedToField() != null ) {
    	wAssignedToField.setText(meta.getRedmineAssignedToField());
    }
    
    wAssignedToInField.setSelection(meta.isRedmineAssignedToInField());
    
    if ( meta.getRedmineSubject() != null ) {
    	wRedmineSubject.setText(meta.getRedmineSubject());
    }
    
    if ( meta.getRedmineSubjectField() != null ) {
    	wSubjectField.setText(meta.getRedmineSubjectField());
    }
    
    wSubjectInField.setSelection(meta.isRedmineSubjectInField());
    
    if ( meta.getRedmineDescription() != null ) {
    	wRedmineDescription.setText(meta.getRedmineDescription());
    }
    
    if ( meta.getRedmineDescriptionField() != null ) {
    	wDescriptionField.setText(meta.getRedmineDescriptionField());
    }
    
    wDescriptionInField.setSelection(meta.isRedmineDescriptionInField());
    
    wAllowDuplications.setSelection(meta.isRedmineAllowDuplicates());

    activeSubjectInfield();
    activeDescriptionInfield();
    activeAssignedToInfield();
  }

  /**
   * Called when the user cancels the dialog.  
   */
  private void cancel() {
    // The "stepname" variable will be the return value for the open() method.  
    // Setting to null to indicate that dialog was cancelled.
    stepname = null;
    // Restoring original "changed" flag on the met aobject
    meta.setChanged( changed );
    // close the SWT dialog window
    dispose();
  }

  /**
   * Called when the user confirms the dialog
   */
  private void ok() {
    // The "stepname" variable will be the return value for the open() method.  
    // Setting to step name from the dialog control
    stepname = wStepname.getText();
    // Setting the  settings to the meta object
    meta.setRedmineUrl(wRedmineURL.getText() );
    meta.setRedmineToken(wRedmineToken.getText() );
    meta.setRedmineProject(wRedmineProject.getText() );
    meta.setRedmineSubject(wRedmineSubject.getText() );
    meta.setRedmineSubjectField(wSubjectField.getText() );
    meta.setRedmineSubjectInField(wSubjectInField.getSelection());
    meta.setRedmineDescription(wRedmineDescription.getText() );
    meta.setRedmineDescriptionField(wDescriptionField.getText() );
    meta.setRedmineDescriptionInField(wDescriptionInField.getSelection());
    meta.setRedmineCategory(wRedmineCategory.getText() );
    meta.setRedmineAssigned(wRedmineAssignedTo.getText() );
    meta.setRedmineAssignedToField(wAssignedToField.getText() );
    meta.setRedmineAssignedToInField(wAssignedToInField.getSelection());
    meta.setRedmineAllowDuplicates(wAllowDuplications.getSelection());
    
    // close the SWT dialog window
    dispose();
  }
  
  	private void activeSubjectInfield() {
  		wSubjectField.setEnabled( wSubjectInField.getSelection() );
  		wlSubjectField.setEnabled( wSubjectInField.getSelection() );
  		wRedmineSubject.setEnabled( !wSubjectInField.getSelection() );
  	}
  	
  	private void activeDescriptionInfield() {
  		wDescriptionField.setEnabled( wDescriptionInField.getSelection() );
  		wlDescriptionField.setEnabled( wDescriptionInField.getSelection() );
  		wRedmineDescription.setEnabled( !wDescriptionInField.getSelection() );
  	}
  	
  	private void activeAssignedToInfield() {
  		wAssignedToField.setEnabled( wAssignedToInField.getSelection() );
  		wlAssignedToField.setEnabled( wAssignedToInField.getSelection() );
  		wRedmineAssignedTo.setEnabled( !wAssignedToInField.getSelection() );
  	}
  	
  	
  	private void setStreamFields() {
  	    if ( !gotPreviousFields ) {
  	    	String subjectfield = wSubjectField.getText();
  	    	String descriptionfield = wDescriptionField.getText();
  	    	String assignedtofield = wAssignedToField.getText();
  	    	wSubjectField.removeAll();
  	    	
  	    	final Map<String, Integer> fields = new HashMap<String, Integer>();

  	    	// Add the currentMeta fields...
  	    	fields.putAll( inputFields );

  	    	Set<String> keySet = fields.keySet();
  	    	List<String> entries = new ArrayList<String>( keySet );

  	    	wSubjectField.setItems( entries.toArray( new String[entries.size()] ) );
  	    	if ( subjectfield != null ) {
  	    		wSubjectField.setText( subjectfield );
  	    	}

  	    	wDescriptionField.setItems( entries.toArray( new String[entries.size()] ) );
  	    	if ( descriptionfield != null ) {
  	    		wDescriptionField.setText( descriptionfield );
  	    	}
  	    	
  	    	wAssignedToField.setItems( entries.toArray( new String[entries.size()] ) );
  	    	if ( assignedtofield != null ) {
  	    		wAssignedToField.setText( assignedtofield );
  	    	}
  	    	
  	    	gotPreviousFields = true;
  	    }
  	}
}
