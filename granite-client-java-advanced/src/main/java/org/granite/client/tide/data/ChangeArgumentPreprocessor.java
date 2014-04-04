package org.granite.client.tide.data;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.granite.client.persistence.Lazy;
import org.granite.client.tide.data.impl.ObjectUtil;
import org.granite.client.tide.server.ArgumentPreprocessor;
import org.granite.client.tide.server.ServerSession;
import org.granite.logging.Logger;

public class ChangeArgumentPreprocessor implements ArgumentPreprocessor {

    private final static Logger log = Logger.getLogger(ChangeArgumentPreprocessor.class);
    
    public Object[] preprocess(ServerSession serverSession, Method method, Object[] args) {
        if (method == null)
            return args;
        
        EntityManager entityManager = null;
        ChangeSetBuilder csb = null;
        
        for (int idx = 0; idx < args.length; idx++) {
    		Annotation[] annotations = method.getParameterAnnotations()[idx];
    		if (annotations == null)
    			continue;
    		boolean found = false;
    		for (Annotation annotation : annotations) {
    			if (annotation.annotationType().equals(Lazy.class)) {
    				found = true;
    				break;
    			}
    		}
    		if (!found)
    			continue;
    		
    		Object entity = args[idx];
    		if (entity == null)
    			continue;
    		
    		if (entityManager == null) {
    			entityManager = PersistenceManager.getEntityManager(entity);
    			csb = new ChangeSetBuilder(entityManager, serverSession);
    		}
    		else if (PersistenceManager.getEntityManager(entity) != entityManager) {
    			throw new IllegalArgumentException("All arguments passed to remote call must be in the same entity manager");
    		}
    		
    		if (entityManager != null && entityManager.getDataManager().hasVersionProperty(entity) && entityManager.getDataManager().getVersion(entity) != null) {
            	log.debug("Building ChangeSet for argument %d: %s", idx, ObjectUtil.toString(entity));
                args[idx] = csb.buildEntityChangeSet(entity);
            }
        }
        return args;
    }
}
