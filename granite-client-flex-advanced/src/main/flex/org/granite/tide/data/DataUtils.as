/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
/**
 * Created by william on 03/10/13.
 */
package org.granite.tide.data {

    import flash.utils.Dictionary;

    import mx.collections.ArrayCollection;

    import mx.collections.IList;

    import org.granite.collections.IMap;

    public class DataUtils {

        public static function diffLists(oldList:Array, newList:Array):Array {
            var listDiff:ListDiff = new ListDiff(oldList, newList);
            listDiff.diff();
            return listDiff.ops;
        }

        public static function diffMaps(oldMap:Object, newMap:Object):Array {
            var oldDic:Dictionary = convertToDictionary(oldMap);
            var newDic:Dictionary = convertToDictionary(newMap);

            var ops:Array = [];
            for (var newKey:Object in newDic) {
                var oldValue:* = oldDic[newKey];
                if (!oldValue)
                    ops.push([ 1, newKey, newDic[newKey]]);
                else if (oldValue != newDic[newKey])
                    ops.push([ 0, newKey, newDic[newKey]]);
            }
            for (var oldKey:Object in oldDic) {
                if (!newDic[oldKey])
                    ops.push([ -1, oldKey, oldDic[oldKey]]);
            }

            return ops;
        }

        private static function convertToDictionary(map:Object):Dictionary {
            var dic:Dictionary = new Dictionary();
            if (map is IMap) {
                for each (var key:Object in IMap(map).keySet)
                    dic[key] = map.get(key);
            }
            else if (map is Array) {
                for each (var entry:Array in map) {
                    dic[entry[0]] = entry[1];
                }
            }
            return dic;
        }
    }
}

import flash.utils.Dictionary;

class ListDiff {

    private var _oldList:Array;
    private var _newList:Array;

    private var _oldi:int = 0, _newi:int = 0;

    private var _ops:Array = [];

    private var _skipOld:Array = [];
    private var _delayedNew:Dictionary = new Dictionary();

    public function ListDiff(oldList:Array, newList:Array):void {
        _oldList = oldList;
        _newList = newList;
    }

    public function get ops():Array {
        return _ops;
    }

    private function moveNew():void {
        _newi++;
        while (_delayedNew[_newi] is Array) {
            var delayedOps:Array = _delayedNew[_newi];
            for each (var op:Object in delayedOps)
                _ops.push(op);
            _newi++;
        }
    }

    private function nextOld():int {
        var i:int = _oldi+1;
        while (_skipOld.indexOf(i) >= 0 && i < _oldList.length)
            i++;
        return i;
    }

    private function nextNew():int {
        return _newi+1;
    }

    private function getIndex(index:int):int {
        for each (var op:Object in _ops) {
            if (op[0] == -1 && op[1] <= index)
                index--;
            else if (op[0] == 1 && op[1] <= index)
                index++;
        }
        return index;
    }

    public function diff():void {
        var i:int;

        for (_oldi = 0; _oldi < _oldList.length; _oldi++) {
            if (_skipOld.indexOf(_oldi) >= 0)
                continue;

            // Same value for current indices on old and new : next and reset current offset
            if (_oldi < _oldList.length && _newi < _newList.length && _oldList[_oldi] == _newList[_newi]) {
                moveNew();
                continue;
            }

            // Lookup same element in new list
            var foundNext:int = -1;
            if (_newi < _newList.length-1) {
                for (i = _newi+1; i < _newList.length; i++) {
                    if (_newList[i] == _oldList[_oldi] && !_delayedNew[i]) {
                        foundNext = i;
                        break;
                    }
                }
            }
            if (foundNext == -1) {
                var oi:int = nextOld();
                var ni:int = nextNew();
                if (oi < _oldList.length && ni < _newList.length && _oldList[oi] == _newList[ni]) {
                    // Element not found in new list but next one matches: update
                    ops.push([ 0, getIndex(_oldi), _newList[_newi] ]);
                    moveNew();
                }
                else {
                    // Element not found in new list: remove
                    ops.push([ -1, getIndex(_oldi), _oldList[_oldi] ]);
                }
            }
            else if (_oldi < _oldList.length-1 && _oldList[_oldi+1] == _newList[_newi]) {
                // Move of element to another position, remove here and schedule an add for final position
                ops.push([ -1, getIndex(_oldi), _oldList[_oldi] ]);
                _delayedNew[foundNext] = [ [ 1, foundNext, _oldList[_oldi] ] ];
            }
            else {
                // Add elements of new list from current index to found index
                while (_newi < foundNext) {
                    var foundOld:int = -1;
                    // Lookup if the element is present later in the old list
                    if (_oldi < _oldList.length-1) {
                        for (i = _oldi+1; i < _oldList.length; i++) {
                            if (_newList[_newi] == _oldList[i] && _skipOld.indexOf(i) < 0) {
                                foundOld = i;
                                break;
                            }
                        }
                    }
                    if (foundOld >= 0) {
                        // Found later, push a remove
                        ops.push([ -1, getIndex(foundOld), _oldList[foundOld] ]);
                        _skipOld.push(foundOld);
                    }

                    ops.push([ 1, _newi, _newList[_newi] ]);
                    moveNew();
                }
                _oldi--;
            }
        }
        // Add missing elements from new list
        while (_newi < _newList.length) {
            ops.push([ 1, _newi, _newList[_newi] ]);
            moveNew();
        }
    }
}
