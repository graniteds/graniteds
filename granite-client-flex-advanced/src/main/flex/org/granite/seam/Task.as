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

package org.granite.seam {

    /*
      Class used to contain a seam taskId for use with SeamRemoteObject.as and SeamOperation.as
      Author Cameron Ingram, Venkat Danda
    */
    public class Task {

        private var _taskId:String;

        public function Task() {
        }

        public function get taskId(): String {
            return _taskId;
        }

        public function set taskId(taskId:String): void {
            _taskId = taskId;
        }
    }
}