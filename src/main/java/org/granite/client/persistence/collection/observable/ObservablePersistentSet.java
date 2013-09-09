package org.granite.client.persistence.collection.observable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.granite.client.collection.ObservableSet;
import org.granite.client.persistence.Loader;
import org.granite.client.persistence.collection.PersistentCollection;
import org.granite.client.persistence.collection.PersistentSet;
import org.granite.client.persistence.collection.UnsafePersistentCollection;

public class ObservablePersistentSet<E> extends ObservableSet<E> implements UnsafePersistentCollection<PersistentSet<E>> {
	
	public ObservablePersistentSet(PersistentSet<E> persistentSet) {
		super(persistentSet);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		internalPersistentCollection().writeExternal(out);
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		internalPersistentCollection().readExternal(in);
	}

	@Override
	public boolean wasInitialized() {
		return internalPersistentCollection().wasInitialized();
	}

	@Override
	public void uninitialize() {
		internalPersistentCollection().uninitialize();
	}

	@Override
	public void initialize() {
		internalPersistentCollection().initialize();
	}

	@Override
	public void initializing() {
		internalPersistentCollection().initializing();
	}

	@Override
	public PersistentCollection clone(boolean uninitialize) {
		return internalPersistentCollection().clone(uninitialize);
	}

	@Override
	public Loader<PersistentCollection> getLoader() {
		return internalPersistentCollection().getLoader();
	}

	@Override
	public void setLoader(Loader<PersistentCollection> loader) {
		internalPersistentCollection().setLoader(loader);
	}

	@Override
	public boolean isDirty() {
		return internalPersistentCollection().isDirty();
	}

	@Override
	public void dirty() {
		internalPersistentCollection().dirty();
	}

	@Override
	public void clearDirty() {
		internalPersistentCollection().clearDirty();
	}

    @Override
    public void addListener(ChangeListener listener) {
    	internalPersistentCollection().addListener(listener);
    }

    @Override
    public void removeListener(ChangeListener listener) {
    	internalPersistentCollection().removeListener(listener);
    }

    @Override
    public void addListener(InitializationListener listener) {
    	internalPersistentCollection().addListener(listener);
    }

    @Override
    public void removeListener(InitializationListener listener) {
    	internalPersistentCollection().removeListener(listener);
    }

	@Override
	public void withInitialized(InitializationCallback callback) {
		internalPersistentCollection().withInitialized(callback);
	}
	
	@Override
	public PersistentSet<E> internalPersistentCollection() {
		return internalPersistentCollection();
	}

}
