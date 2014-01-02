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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.granite.builder.util.ProjectUtil;

/**
 * @author Franck WOLFF
 */
public class GraniteRebuildJob extends Job {

	public static final String RESET_KEY = "reset";
	
    private final IProject project;
    private final boolean reset;
    
    public GraniteRebuildJob(IProject project) {
    	this(project, false);
    }
    
    public GraniteRebuildJob(IProject project, boolean reset) {
        super("Granite Rebuild Job");
        this.project = project;
        this.reset = reset;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
    	Map<String, String> args = new HashMap<String, String>();
    	if (reset)
    		args.put(RESET_KEY, null);
        try {
            project.build(IncrementalProjectBuilder.FULL_BUILD, GraniteBuilder.GRANITE_BUILDER_ID, args, monitor);
        } catch (CoreException e) {
            return ProjectUtil.createErrorStatus("Granite Rebuild Failed", e);
        }
        return Status.OK_STATUS;
    }
}
