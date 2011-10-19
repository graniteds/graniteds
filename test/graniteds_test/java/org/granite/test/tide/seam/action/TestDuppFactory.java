package org.granite.test.tide.seam.action;

import org.granite.test.tide.TestDataUpdatePostprocessor;
import org.granite.tide.data.DataUpdatePostprocessor;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.ServletLifecycle;


@Name("duppFactory")
@BypassInterceptors
public class TestDuppFactory {

	@Factory(value="org.granite.tide.seam.data.dataUpdatePreprocessor", scope=ScopeType.EVENT, autoCreate=true)
	public DataUpdatePostprocessor buildDupp() {
		if (ServletLifecycle.getServletContext().getAttribute("dupp") != null)
			return new TestDataUpdatePostprocessor();
		return null;
	}
}
