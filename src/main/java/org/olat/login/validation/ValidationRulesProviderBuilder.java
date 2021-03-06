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
package org.olat.login.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Initial date: 19 Jun 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ValidationRulesProviderBuilder {
	
	private final List<ValidationRule> rules = new ArrayList<>();
	
	public ValidationRulesProviderBuilder add(ValidationRule rule) {
		rules.add(rule);
		return this;
	}

	public ValidationRulesProvider create() {
		return new ValidationRulesProviderImpl(rules);
	}

	private static class ValidationRulesProviderImpl implements ValidationRulesProvider {
		
		private final List<ValidationRule> rules;
		
		private ValidationRulesProviderImpl(List<ValidationRule> rules) {
			this.rules = new ArrayList<>(rules);
		}

		@Override
		public List<ValidationRule> getRules() {
			return rules;
		}

	}
}
