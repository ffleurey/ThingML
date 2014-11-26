/**
 * Copyright (C) 2014 SINTEF <franck.fleurey@sintef.no>
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingml.eclipse.ui.commands;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;
import org.thingml.compilers.ThingMLCompiler;
import org.thingml.compilers.ThingMLCompilerRegistry;

public class DynamicCompilerMenu extends  CompoundContributionItem implements IWorkbenchContribution {

	@Override
	protected IContributionItem[] getContributionItems() {
		
		
		IContributionItem[] list = new IContributionItem[ThingMLCompilerRegistry.getInstance().getCompilers().size()];
		int i=0;
		Map<String, String> parms;
		
		for (ThingMLCompiler c : ThingMLCompilerRegistry.getInstance().getCompilers()) {
			parms = new HashMap<String, String>();
			parms.put("org.thingml.eclipse.ui.commandParameterCompilerName", c.getName());
			list[i] =  new CommandContributionItem(new CommandContributionItemParameter(serviceLocator, "itemid_"+i, "thingml.compile", parms, null, null, null, c.getName(), null, c.getDescription(), CommandContributionItem.STYLE_PUSH, null, true));
			i++;
		}
		
		return list;
		
	}
	
	// Service locator to located the handler service.
	private IServiceLocator serviceLocator;

	@Override
	public void initialize(IServiceLocator serviceLocator) {
		this.serviceLocator = serviceLocator;
	}
	
	
	

}
