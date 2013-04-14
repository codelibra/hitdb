/*
    Hit is a high speed transactional database for handling millions
    of updates with comfort and ease. 

    Copyright (C) 2013  Balraja Subbiah

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.hit.db.query.operators;

import java.util.regex.Pattern;

import org.hit.db.model.Queryable;

/**
 * @author Balraja Subbiah
 */
public class StringComparision extends AbstractCondition
{

    private final Pattern myPattern;
    
    /**
     * CTOR
     */
    public StringComparision(String columnName, String regex)
    {
        super(columnName);
        myPattern = Pattern.compile(regex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid(Queryable record)
    {
        Object fieldValue = getValue(record);
        return fieldValue != null ? myPattern.matcher(fieldValue.toString())
                                             .matches()
                                  : false;
    }
}