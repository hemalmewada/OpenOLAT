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
package org.olat.ims.qti21.ui.editor.metadata;

import org.olat.modules.qpool.MetadataSecurityCallback;

/**
 * Allow to change metadata and taxonomy.
 * 
 * Initial date: 8 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class AssessmentItemEditorMetadataSecurityCallback implements MetadataSecurityCallback {
	
	private final boolean readOnly;
	
	AssessmentItemEditorMetadataSecurityCallback(boolean readOnly) {
		this.readOnly = readOnly;
	}

	@Override
	public boolean canEditMetadata() {
		return !readOnly;
	}

	@Override
	public boolean canRemoveTaxonomy() {
		return true;
	}

	@Override
	public boolean canChangeVersion() {
		return false;
	}

	@Override
	public boolean canEditAuthors() {
		return false;
	}

	@Override
	public boolean canViewReviews() {
		return false;
	}
}
