/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */

package org.granite.builder;

import static org.granite.builder.GraniteBuilder.GRANITE_BUILDER_ID;
import static org.granite.builder.GraniteBuilder.FLEX_BUILDER_ID;
import static org.granite.builder.GraniteBuilder.JAVA_BUILDER_ID;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Franck WOLFF
 */
public class GraniteNature implements IProjectNature {

    public static final String NATURE_ID = "org.granite.builder.granitenature";

    private static final Comparator<ICommand> BUILDER_COMPARATOR = new Comparator<ICommand>() {

    	// java -> granite [-> flex]
		@Override
		public int compare(ICommand c1, ICommand c2) {
			if (GRANITE_BUILDER_ID.equals(c1.getBuilderName())) {
				if (JAVA_BUILDER_ID.equals(c2.getBuilderName()))
					return 1;
				if (FLEX_BUILDER_ID.equals(c2.getBuilderName()))
					return -1;
			}
			else if (JAVA_BUILDER_ID.equals(c1.getBuilderName())) {
				if (GraniteBuilder.GRANITE_BUILDER_ID.equals(c2.getBuilderName()) || FLEX_BUILDER_ID.equals(c2.getBuilderName()))
					return -1;
			}
			else if (FLEX_BUILDER_ID.equals(c1.getBuilderName())) {
				if (GRANITE_BUILDER_ID.equals(c2.getBuilderName()) || JAVA_BUILDER_ID.equals(c2.getBuilderName()))
					return 1;
			}
			return 0;
		}
    };
    
    private IProject project;

    @Override
	public void configure() throws CoreException {
        IProjectDescription desc = project.getDescription();
        ICommand[] commands = desc.getBuildSpec();

        for (ICommand command : commands) {
            if (command.getBuilderName().equals(GRANITE_BUILDER_ID))
                return;
        }

        ICommand[] newCommands = new ICommand[commands.length + 1];
        System.arraycopy(commands, 0, newCommands, 0, commands.length);
        ICommand command = desc.newCommand();
        command.setBuilderName(GRANITE_BUILDER_ID);
        newCommands[newCommands.length - 1] = command;
        Arrays.sort(newCommands, BUILDER_COMPARATOR);
        desc.setBuildSpec(newCommands);
        project.setDescription(desc, null);
    }

    @Override
	public void deconfigure() throws CoreException {
        IProjectDescription description = project.getDescription();
        ICommand[] commands = description.getBuildSpec();

        for (int i = 0; i < commands.length; i++) {
            if (commands[i].getBuilderName().equals(GRANITE_BUILDER_ID)) {
                ICommand[] newCommands = new ICommand[commands.length - 1];
                System.arraycopy(commands, 0, newCommands, 0, i);
                System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
                description.setBuildSpec(newCommands);
                return;
            }
        }
    }

    @Override
	public IProject getProject() {
        return project;
    }

    @Override
	public void setProject(IProject project) {
        this.project = project;
    }
}
