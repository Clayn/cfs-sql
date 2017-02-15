/*
 * Copyright (C) 2016 Clayn <clayn_osmato@gmx.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.bplaced.clayn.cfs.impl.sql.err;

import net.bplaced.clayn.cfs.err.CFSException;

/**
 *
 * @author Clayn <clayn_osmato@gmx.de>
 */
public class ModificationDetectionException extends CFSException
{

    public ModificationDetectionException()
    {
    }

    public ModificationDetectionException(String message)
    {
        super(message);
    }

    public ModificationDetectionException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ModificationDetectionException(Throwable cause)
    {
        super(cause);
    }

    public ModificationDetectionException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
