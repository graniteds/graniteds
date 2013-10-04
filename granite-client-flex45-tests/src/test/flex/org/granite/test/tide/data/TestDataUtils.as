/**
 * Created by william on 03/10/13.
 */
package org.granite.test.tide.data {

    import flash.utils.getQualifiedClassName;

    import mx.collections.ArrayCollection;
    import mx.collections.IList;
    import mx.core.IUID;

    import org.flexunit.Assert;
    import org.granite.IValue;
    import org.granite.meta;

    import org.granite.test.tide.Classification;
    import org.granite.tide.EntityDescriptor;
    import org.granite.tide.IEntity;
    import org.granite.tide.IPropertyHolder;
    import org.granite.tide.Tide;
    import org.granite.tide.data.ChangeRef;
    import org.granite.tide.data.CollectionChange;
    import org.granite.tide.data.CollectionChanges;
    import org.granite.tide.data.DataUtils;
    import org.granite.util.Enum;

    use namespace meta;

    public class TestDataUtils {

        [Test]
        public function testDiffRandom():void {
            for (var count:int = 0; count < 500; count++) {
                var oldSize = Math.floor(Math.random()*200);
                var newSize = Math.floor(Math.random()*200);

                var max:int = Math.max(oldSize,  newSize);
                var c:Array = [];
                for (var i:int = 0; i < max; i++)
                    c[i] = new Classification(null, null, "C" + i);

                var oldList:IList = new ArrayCollection();
                var newList:IList = new ArrayCollection();

                for (var j:int = 0; j < oldSize; j++)
                    oldList.addItem(c[Math.floor(Math.random()*oldSize)]);
                for (var k:int = 0; k < newSize; k++)
                    newList.addItem(c[Math.floor(Math.random()*newSize)]);

                var ops:Array = DataUtils.diffLists(oldList.toArray(), newList.toArray());

                checkListDiff(oldList, newList, ops);
            }
        }

        private function checkListDiff(oldList:IList, newList:IList, ops:Array):void {
//            StringBuilder sb = new StringBuilder();
//            for (Classification c : oldList)
//            sb.append(c.getUid()).append(" ");
//            System.out.println("Initial : " + sb);
//            sb = new StringBuilder();
//            for (Classification c : newList)
//            sb.append(c.getUid()).append(" ");
//            System.out.println("Expected: " + sb);
//            System.out.printf("%d operations: ", ops.size());
//            for (Object[] op : ops)
//            System.out.printf("%d %d %s  ", op[0], op[1], ((Classification)op[2]).getUid());
//            System.out.println();

            var checkList:ArrayCollection = new ArrayCollection(oldList.toArray());
            for each (var op:Object in ops) {
                if (op[0] == -1)
                    checkList.removeItemAt(op[1]);
                else if (op[0] == 1)
                    checkList.addItemAt(op[2], op[1]);
                else if (op[0] == 0)
                    checkList.setItemAt(op[2], op[1]);
            }

//            sb = new StringBuilder();
//            for (Classification c : checkList)
//            sb.append(c.getUid()).append(" ");
//            System.out.println("Result  : " + sb);

            Assert.assertEquals("Expected list size", newList.length, checkList.length);
            for (var i:int = 0; i < checkList.length; i++)
                Assert.assertEquals("Element " + i, newList.getItemAt(i).uid, checkList.getItemAt(i).uid);
        }


        public static function checkListChangeSet(coll:IList, collChanges:CollectionChanges, collSnapshot:Array):void {
            var checkList:ArrayCollection = new ArrayCollection(collSnapshot);
            for each (var collChange:CollectionChange in collChanges.changes) {
                if (collChange.type == -1)
                    checkList.removeItemAt(int(collChange.key));
                else if (collChange.type == 1)
                    checkList.addItemAt(collChange.value, int(collChange.key));
                else if (collChange.type == 0)
                    checkList.setItemAt(collChange.value, int(collChange.key));
            }

            Assert.assertEquals("Expected list length", coll.length, checkList.length);
            for (var i:int = 0; i < checkList.length; i++)
                Assert.assertTrue("Element " + i, objectEquals(coll.getItemAt(i), checkList.getItemAt(i)));
        }


        public static function objectEquals(obj1:Object, obj2:Object):Boolean {
            if ((obj1 is IPropertyHolder && obj2 is IEntity) || (obj1 is IEntity && obj2 is IPropertyHolder))
                return false;

            if (obj1 is IUID && obj2 is IUID && getQualifiedClassName(obj1) == getQualifiedClassName(obj2)) {
                try {
                    if (obj1 is IEntity && (!obj1.meta::isInitialized() || !obj2.meta::isInitialized())) {
                        // Compare with identifier for uninitialized entities
                        var edesc:EntityDescriptor = Tide.getInstance().getEntityDescriptor(IEntity(obj1));
                        if (edesc.idPropertyName != null)
                            return objectEquals(obj1[edesc.idPropertyName], obj2[edesc.idPropertyName]);
                    }
                }
                catch (e:ReferenceError) {
                    // Entity class does not implement meta::isInitialized, consider as true
                }
                return IUID(obj1).uid == IUID(obj2).uid;
            }

            if (obj1 is ChangeRef && obj2 is IEntity)
                return ChangeRef(obj1).isForEntity(obj2);

            if (obj1 is IEntity && obj2 is ChangeRef)
                return ChangeRef(obj2).isForEntity(obj1);

            if (obj1 is Enum && obj2 is Enum && obj1.equals(obj2))
                return true;

            if (obj1 is IValue && obj2 is IValue && obj1.equals(obj2))
                return true;

            return obj1 === obj2;
        }
    }
}
