/*
 GRANITE DATA SERVICES
 Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

 This file is part of Granite Data Services.

 Granite Data Services is free software; you can redistribute it and/or modify
 it under the terms of the GNU Library General Public License as published by
 the Free Software Foundation; either version 2 of the License, or (at your
 option) any later version.

 Granite Data Services is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 for more details.

 You should have received a copy of the GNU Library General Public License
 along with this library; if not, see <http://www.gnu.org/licenses/>.
 */

package test.granite.components
{

import flash.events.Event;

import mx.collections.Sort;
import mx.controls.DataGrid;
import mx.core.EventPriority;
import mx.data.utils.Managed;
import mx.events.CollectionEvent;
import mx.events.CollectionEventKind;
import mx.events.DataGridEvent;
import mx.events.DataGridEventReason;

import org.granite.tide.IEntity;
import org.granite.tide.validators.TideEntityValidator;

/**
 * @author William DRAI
 */
[Event(name="itemEditSelect", type="mx.events.DataGridEvent")]
[Event(name="itemEditCommit", type="mx.events.DataGridEvent")]
public class DataGrid extends mx.controls.DataGrid
{

    public function DataGrid()
    {
        super();

        super.addEventListener( Event.CHANGE, changeHandler );
        //            super.addEventListener(FlexEvent.VALUE_COMMIT, valueCommitHandler);
        super.addEventListener( DataGridEvent.ITEM_EDIT_BEGINNING, itemEditBeginningHandler, false, EventPriority.BINDING );
        super.addEventListener( DataGridEvent.ITEM_EDIT_BEGIN, itemEditBeginHandler, false, EventPriority.BINDING );
        super.addEventListener( DataGridEvent.ITEM_EDIT_END, itemEditEndHandler, false, EventPriority.BINDING );
    }

    override public function addEventListener(
            type:String, listener:Function, useCapture:Boolean = false,
            priority:int = 0, useWeakReference:Boolean = false ):void
    {

        if( type === DataGridEvent.HEADER_RELEASE && !hasEventListener( type ) )
        {
            super.addEventListener( type, preHeaderReleaseHandler, useCapture, priority, useWeakReference );
        }

        super.addEventListener( type, listener, useCapture, priority, useWeakReference );
    }

    private function preHeaderReleaseHandler( event:DataGridEvent ):void
    {
        if( !event.isDefaultPrevented() )
        {
            var column:DataGridColumn = columns[event.columnIndex];
            if( column.sortable && column.dataField.indexOf( '.' ) != -1 )
            {
                var s:Sort = collection.sort;
                collection.sort = new PropertySort( column.sortCompareFunction );
                if( s )
                {
                    collection.sort.fields = s.fields;
                }
            }
        }
    }


    private var itemEditCommit:Boolean = false;


    private function changeHandler( event:Event ):void
    {
        var selectEvent:DataGridEvent = new DataGridEvent( "itemEditSelect" );
        dispatchEvent( selectEvent );
    }


    private function itemEditBeginningHandler( event:DataGridEvent ):void
    {
        if( event.itemRenderer == null || event.itemRenderer.data == null )
        {
            event.preventDefault();
            return;
        }
        var itemEditor:Object = DataGridColumn( columns[event.columnIndex] ).itemEditor;
        if( itemEditor is ItemEditorFactory )
        {
            var ief:ItemEditorFactory = ItemEditorFactory( itemEditor );
            ief.data = event.itemRenderer.data;
            ief.dataField = event.dataField;
            ief.validHandler = null;
            ief.invalidHandler = null;
        }

        var selectEvent:DataGridEvent = new DataGridEvent( "itemEditSelect" );
        dispatchEvent( selectEvent );
    }

    private function itemEditBeginHandler( event:DataGridEvent ):void
    {
    }

    private function itemEditEndHandler( event:DataGridEvent ):void
    {
        itemEditCommit = false;
        //            var data:Object = itemEditorInstance ? itemEditorInstance.data : null;
        var data:Object = event.itemRenderer.data;
        var entity:IEntity = data is IEntity ? IEntity( data ) : null;

        if( event.reason == DataGridEventReason.CANCELLED || itemEditorInstance == null )
        {
            if( entity )
            {
                Managed.resetEntity( entity );
            }
            return;
        }

        var c:DataGridColumn = columns[event.columnIndex];
        var obj:Object = data;
        var properties:Array = c.dataField.split( /\./ );
        if( properties.length > 1 )
        {
            for( var i:uint = 0; i < properties.length - 1 && obj != null; i++ )
            {
                obj = obj[properties[i]];
            }
        }

        // Ignore if no change made
        if( obj == null || obj[properties[properties.length - 1]] == itemEditorInstance[Object( c.itemEditor ).editorDataField] )
        {
            event.reason = DataGridEventReason.CANCELLED;
            if( entity )
            {
                Managed.resetEntity( entity );
            }
            return;
        }

        // Validate data
        if( itemEditorInstance )
        {
            var res:Array = ItemEditorFactory.validateAll( itemEditorInstance );
            if( res && res.length > 0 )
            {
                event.preventDefault();
                return;
            }
        }

        if( entity )
        {
            var sv:TideEntityValidator = new TideEntityValidator();
            sv.entity = entity;
            sv.property = event.dataField;
            sv.listener = event.currentTarget.itemEditorInstance;
        }

        // Force field update and triggers remote data update when pressing Return
        obj[properties[properties.length - 1]] = itemEditorInstance[Object( c.itemEditor ).editorDataField];
        var commitEvent:DataGridEvent = new DataGridEvent( "itemEditCommit" );
        dispatchEvent( commitEvent );
        itemEditCommit = true;
        event.preventDefault();
    }


    override protected function collectionChangeHandler( event:Event ):void
    {
        if( event is CollectionEvent )
        {
            var ceEvent:CollectionEvent = event as CollectionEvent;
            if( ceEvent.kind == CollectionEventKind.UPDATE )
            {
                for( var i:int = 0; i < ceEvent.items.length; i++ )
                {
                    if( editedItemPosition
                            && itemEditorInstance.data === ceEvent.items[i].target
                            && ceEvent.items[i].property == null )
                    {
                        var ce:CollectionEvent = ceEvent.clone() as CollectionEvent;
                        ce.kind = CollectionEventKind.REPLACE;
                        ce.location = this.editedItemPosition.rowIndex;
                        super.collectionChangeHandler( ce );
                        break;
                    }
                }
            }
        }

        super.collectionChangeHandler( event );
    }
}
}
