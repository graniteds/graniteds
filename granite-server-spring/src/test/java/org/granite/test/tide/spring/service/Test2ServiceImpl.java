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
package org.granite.test.tide.spring.service;

import java.math.BigDecimal;

import org.granite.tide.data.DataEnabled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service("test2Service")
@Transactional
@DataEnabled(topic="testTopic")
public class Test2ServiceImpl implements Test2Service, Test3Service<String, BigDecimal> {
	
    @Override
    public String test(String name, BigDecimal value) {
        if (value.intValue() == 2)
            return "ok";
        return "nok";
    }
}
