/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.portfolio.ui.media;

import java.io.File;
import java.util.List;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.image.ImageFormItem;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.ContentEditorModule;
import org.olat.modules.ceditor.ContentEditorXStream;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.model.MediaPart;
import org.olat.modules.portfolio.model.StandardMediaRenderingHints;
import org.olat.modules.portfolio.ui.editor.PageEditorController;
import org.olat.modules.portfolio.ui.editor.PageElementEditorController;
import org.olat.modules.portfolio.ui.editor.event.ChangePartEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImageMediaEditorController extends FormBasicController implements PageElementEditorController {
	
	private static final String[] titlePositionKeys = new String[] {
			ImageTitlePosition.above.name(), ImageTitlePosition.top.name(), ImageTitlePosition.centered.name(), ImageTitlePosition.bottom.name()
		};
	private static final String[] alignmentKeys = new String[]{
			ImageHorizontalAlignment.left.name(), ImageHorizontalAlignment.middle.name(), ImageHorizontalAlignment.right.name()
		};
	private static final String[] sizeKeys = new String[] {
			ImageSize.none.name(), ImageSize.small.name(), ImageSize.medium.name(), ImageSize.fill.name()
		};
	private static final String[] onKeys = new String[] { "on" };
	
	private SingleSelection sizeEl;
	private SingleSelection styleEl;
	private SingleSelection alignmentEl;
	private MultipleSelectionElement sourceEl;
	private MultipleSelectionElement descriptionEnableEl;
	
	private TextElement titleEl;
	private SingleSelection titlePositionEl;
	private SingleSelection titleStyleEl;
	
	private TextElement captionEl;
	private TextElement descriptionEl;
	
	private ImageMediaController imagePreview;
	
	private MediaPart mediaPart;
	private boolean editMode;
	
	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private ContentEditorModule contentEditorModule;
	
	public ImageMediaEditorController(UserRequest ureq, WindowControl wControl, MediaPart mediaPart) {
		super(ureq, wControl, "editor_image", Util.createPackageTranslator(PageEditorController.class, ureq.getLocale()));
		this.mediaPart = mediaPart;
		initForm(ureq);
	}
	
	@Override
	public boolean isEditMode() {
		return editMode;
	}

	@Override
	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
		alignmentEl.setVisible(editMode);
		flc.getFormItemComponent().contextPut("editMode", Boolean.valueOf(editMode));
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Media media = mediaPart.getMedia();
		File mediaDir = new File(FolderConfig.getCanonicalRoot(), media.getStoragePath());
		File mediaFile = new File(mediaDir, media.getRootFilename());
		ImageFormItem imageEl = new ImageFormItem(ureq.getUserSession(), "image");
		imageEl.setMedia(mediaFile);
		formLayout.add("image", imageEl);
		
		imagePreview = new ImageMediaController(ureq, getWindowControl(), mediaPart, new StandardMediaRenderingHints());
		listenTo(imagePreview);
		((FormLayoutContainer)formLayout).getFormItemComponent().put("imagePreview", imagePreview.getInitialComponent());
		
		titleEl = uifactory.addTextElement("image.title", 255, null, formLayout);
		titleEl.setPlaceholderKey("image.title.placeholder", null);
		titleEl.addActionListener(FormEvent.ONCHANGE);
		
		String[] titlePositionValues = new String[] {
				translate("image.title.position.above"), translate("image.title.position.top"),
				translate("image.title.position.centered"), translate("image.title.position.bottom")
			};
		titlePositionEl = uifactory.addDropdownSingleselect("image.title.position", "image.title.position", formLayout, titlePositionKeys, titlePositionValues, null);
		titlePositionEl.addActionListener(FormEvent.ONCHANGE);
		
		List<String> titleStyleList = contentEditorModule.getImageTitleStyleList();
		String[] titleStyles = titleStyleList.toArray(new String[titleStyleList.size()]);
		String[] titleStylesValues = new String[titleStyles.length];
		for(int i=titleStyles.length; i-->0; ) {
			String stylename = translate("image.title.style." + titleStyles[i]);
			if(stylename.length() < 32 && !stylename.startsWith("image.title.")) {
				titleStylesValues[i] = stylename;
			} else {
				titleStylesValues[i] = titleStyles[i];
			}
		}
		titleStyleEl = uifactory.addDropdownSingleselect("image.title.style", "image.title.style", formLayout, titleStyles, titleStylesValues, null);
		titleStyleEl.addActionListener(FormEvent.ONCHANGE);

		captionEl = uifactory.addTextElement("image.caption", 255, null, formLayout);
		captionEl.setPlaceholderKey("image.caption.placeholder", null);
		captionEl.addActionListener(FormEvent.ONCHANGE);

		String[] alignmentValues = new String[] {
				translate("image.align.left"), translate("image.align.middle"), translate("image.align.right")	
		};
		alignmentEl = uifactory.addDropdownSingleselect("image.align", "image.align", formLayout, alignmentKeys, alignmentValues, null);
		alignmentEl.addActionListener(FormEvent.ONCHANGE);
		
		String[] sizeValues = new String[] {
				translate("image.size.none"), translate("image.size.small"), translate("image.size.medium"), translate("image.size.fill")	
		};
		sizeEl = uifactory.addDropdownSingleselect("image.size", "image.size", formLayout, sizeKeys, sizeValues, null);
		sizeEl.addActionListener(FormEvent.ONCHANGE);
		
		List<String> styleList = contentEditorModule.getImageStyleList();
		String[] styles = styleList.toArray(new String[styleList.size()]);
		String[] stylesValues = new String[styles.length];
		for(int i=styles.length; i-->0; ) {
			String stylename = translate("image.style." + styles[i]);
			if(stylename.length() < 32 && !stylename.startsWith("image.")) {
				stylesValues[i] = stylename;
			} else {
				stylesValues[i] = styles[i];
			}
		}
		styleEl = uifactory.addDropdownSingleselect("image.style", "image.style", formLayout, styles, stylesValues, null);
		styleEl.addActionListener(FormEvent.ONCHANGE);

		String[] licenseValues = new String[] { translate("image.origin.show") };
		sourceEl = uifactory.addCheckboxesHorizontal("image.origin", "image.origin", formLayout, onKeys, licenseValues);
		sourceEl.addActionListener(FormEvent.ONCHANGE);
		
		String[] descriptionValues = new String[] { translate("image.description.show") };
		descriptionEnableEl = uifactory.addCheckboxesHorizontal("image.description", "image.description", formLayout, onKeys, descriptionValues);
		descriptionEnableEl.addActionListener(FormEvent.ONCHANGE);
		
		descriptionEl = uifactory.addTextAreaElement("image.description.content", 4, 60, null, formLayout);
		descriptionEl.addActionListener(FormEvent.ONCHANGE);
		descriptionEl.setVisible(false);
		
		if(StringHelper.containsNonWhitespace(mediaPart.getLayoutOptions())) {
			ImageSettings settings = ContentEditorXStream.fromXml(mediaPart.getLayoutOptions(), ImageSettings.class);
			if(settings.getAlignment() != null) {
				for(String alignmentKey:alignmentKeys) {
					if(settings.getAlignment().name().equals(alignmentKey)) {
						alignmentEl.select(alignmentKey, true);
					}
				}
			}
			if(settings.getSize() != null) {
				for(String sizeKey:sizeKeys) {
					if(settings.getSize().name().equals(sizeKey)) {
						sizeEl.select(sizeKey, true);
					}
				}
			}
			
			if(StringHelper.containsNonWhitespace(settings.getStyle())) {
				for(int i=styles.length; i-->0; ) {
					if(styles[i].equals(settings.getStyle())) {
						styleEl.select(styles[i], true);
					}
				}
			}
			
			sourceEl.select(onKeys[0], settings.isShowSource());
			descriptionEnableEl.select(onKeys[0], settings.isShowDescription());
			descriptionEl.setValue(settings.getDescription());
			descriptionEl.setVisible(descriptionEnableEl.isAtLeastSelected(1));
			
			if(StringHelper.containsNonWhitespace(settings.getTitle())) {
				titleEl.setValue(settings.getTitle());
				
				if(StringHelper.containsNonWhitespace(settings.getTitleStyle())) {
					for(String titleStyle:titleStyles) {
						if(titleStyle.equals(settings.getTitleStyle())) {
							titleStyleEl.select(titleStyle, true);
						}
					}
				}
				
				if(settings.getTitlePosition() != null) {
					titlePositionEl.select(settings.getTitlePosition().name(), true);
				}
			}
			
			if(StringHelper.containsNonWhitespace(settings.getCaption())) {
				captionEl.setValue(settings.getCaption());
			}
			
			if(StringHelper.containsNonWhitespace(settings.getDescription())) {
				descriptionEl.setValue(settings.getDescription());
			}
		} else {
			if(StringHelper.containsNonWhitespace(mediaPart.getMedia().getDescription())) {
				captionEl.setValue(mediaPart.getMedia().getDescription());
			}
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(descriptionEnableEl == source) {
			descriptionEl.setVisible(descriptionEnableEl.isAtLeastSelected(1));
			doSaveSettings(ureq);
		} else if(styleEl == source || alignmentEl == source || sizeEl == source
				|| sourceEl == source || captionEl == source || descriptionEl == source
				|| titleEl == source || titleStyleEl == source || titlePositionEl == source) {
			doSaveSettings(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doSaveSettings(UserRequest ureq) {
		ImageSettings settings;
		if(StringHelper.containsNonWhitespace(mediaPart.getLayoutOptions())) {
			settings = ContentEditorXStream.fromXml(mediaPart.getLayoutOptions(), ImageSettings.class);
		} else {
			settings = new ImageSettings();
		}
		
		//title
		if(StringHelper.containsNonWhitespace(titleEl.getValue())) {
			settings.setTitle(titleEl.getValue());
			if(titleStyleEl.isOneSelected()) {
				settings.setTitleStyle(titleStyleEl.getSelectedKey());
			} else {
				settings.setTitleStyle(null);
			}
			
			if(titlePositionEl.isOneSelected()) {
				settings.setTitlePosition(ImageTitlePosition.valueOf(titlePositionEl.getSelectedKey()));
			} else {
				settings.setTitlePosition(null);
			}
		} else {
			settings.setTitle(null);
			settings.setTitleStyle(null);
			settings.setTitlePosition(null);
		}
		
		if(StringHelper.containsNonWhitespace(captionEl.getValue())) {
			settings.setCaption(captionEl.getValue());
		} else {
			settings.setCaption(null);
		}
		
		if(alignmentEl.isOneSelected()) {
			ImageHorizontalAlignment alignment = ImageHorizontalAlignment.valueOf(alignmentEl.getSelectedKey());
			settings.setAlignment(alignment);
		}
		if(sizeEl.isOneSelected()) {
			ImageSize size = ImageSize.valueOf(sizeEl.getSelectedKey());
			settings.setSize(size);
		}
		settings.setShowSource(sourceEl.isAtLeastSelected(1));
		
		settings.setShowDescription(descriptionEnableEl.isAtLeastSelected(1));
		if(descriptionEl.isVisible()) {
			settings.setDescription(descriptionEl.getValue());
		} else {
			settings.setDescription(null);
		}
		
		if(styleEl.isOneSelected()) {
			settings.setStyle(styleEl.getSelectedKey());
		} else {
			settings.setStyle(null);
		}
		
		String settingsXml = ContentEditorXStream.toXml(settings);
		mediaPart.setLayoutOptions(settingsXml);
		mediaPart = portfolioService.updatePart(mediaPart);
		
		removeAsListenerAndDispose(imagePreview);
		imagePreview = new ImageMediaController(ureq, getWindowControl(), mediaPart, new StandardMediaRenderingHints());
		listenTo(imagePreview);
		flc.getFormItemComponent().put("imagePreview", imagePreview.getInitialComponent());
		
		fireEvent(ureq, new ChangePartEvent(mediaPart));
	}
}
