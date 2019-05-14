package es.jcozar.pdi.redmine.plugin.utils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.pentaho.di.core.Const;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.widget.LabelText;

public class SwtUtils {
	
	public static Composite addTab(Composite parent, CTabFolder wTabFolder, String label, Control top) {
	
		CTabItem wGeneralTab = new CTabItem( wTabFolder, SWT.NONE );
	    wGeneralTab.setText( label );
	
	    Composite result = new Composite( wTabFolder, SWT.NONE );
	    PropsUI.getInstance().setLook( result );
	
	    FormLayout fileLayout = new FormLayout();
	    fileLayout.marginWidth = 3;
	    fileLayout.marginHeight = 3;
	    result.setLayout( fileLayout );
	    
	    FormData fdGeneralComp = new FormData();
	    fdGeneralComp.left = new FormAttachment( 0, 0 );
	    fdGeneralComp.top = new FormAttachment( top, Const.MARGIN );
	    fdGeneralComp.right = new FormAttachment( 100, 0 );
	    fdGeneralComp.bottom = new FormAttachment( 100, 0 );
	    result.setLayoutData( fdGeneralComp );

	    result.layout();
	    wGeneralTab.setControl( result );
	    
	    return result;
	}

	public static Group addGroup(Composite parent, String label, Control top) {
		
		Group result = new Group( parent, SWT.SHADOW_ETCHED_IN );
		result.setText(label );
	    FormLayout SettingsLayout = new FormLayout();
	    SettingsLayout.marginWidth = 3;
	    SettingsLayout.marginHeight = 3;
	    result.setLayout( SettingsLayout );
	    PropsUI.getInstance().setLook( result );
	    FormData fdSettings = new FormData();
	    fdSettings.left = new FormAttachment( 0, 0 );
	    fdSettings.right = new FormAttachment( 100, 0 );
	    fdSettings.top = new FormAttachment( top, Const.MARGIN );
	    result.setLayoutData( fdSettings );
	    
	    return result;
	}
	
	public static LabelText addLabelText(Composite parent, String label, Control top) {
		
		LabelText result = new LabelText( parent, label, null);
	    FormData formData = new FormData();
	    formData.left = new FormAttachment( 0, 0 );
	    formData.right = new FormAttachment( 100, 0 );
	    formData.top = new FormAttachment( top, Const.MARGIN );
	    result.setLayoutData( formData );
	    PropsUI.getInstance().setLook(result);
	    
	    return result;
	}
	
	public static Button addCheckBox(Composite parent, String label, Control top) {
		
		Label swtLabel = new Label( parent, SWT.RIGHT );
		swtLabel.setText( label );
	    FormData fdlDescriptionInField = new FormData();
	    fdlDescriptionInField.left = new FormAttachment( 0, 0 );
	    fdlDescriptionInField.top = new FormAttachment( top, Const.MARGIN );
	    fdlDescriptionInField.right = new FormAttachment( Const.MIDDLE_PCT, -Const.MARGIN );
	    swtLabel.setLayoutData( fdlDescriptionInField );
	    PropsUI.getInstance().setLook(swtLabel);
	    
	    Button result = new Button( parent, SWT.CHECK );
	    FormData fdDescriptionInField = new FormData();
	    fdDescriptionInField.left = new FormAttachment( Const.MIDDLE_PCT, 0 );
	    fdDescriptionInField.top = new FormAttachment( top, Const.MARGIN );
	    fdDescriptionInField.right = new FormAttachment( 100, 0 );
	    result.setLayoutData( fdDescriptionInField );
	    PropsUI.getInstance().setLook(result);
	    
	    return result;
	}
}
