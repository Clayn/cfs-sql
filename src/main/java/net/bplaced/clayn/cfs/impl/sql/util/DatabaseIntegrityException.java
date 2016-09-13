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
package net.bplaced.clayn.cfs.impl.sql.util;

/**
 * Exception thrown in cases where the integrity of the databse can't be ensured
 * anymore.
 *
 * @author Clayn <clayn_osmato@gmx.de>
 * @since 0.2.0
 */
public class DatabaseIntegrityException extends RuntimeException
{

    public DatabaseIntegrityException()
    {
    }

    public DatabaseIntegrityException(String message)
    {
        super(message);
    }

    public DatabaseIntegrityException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public DatabaseIntegrityException(Throwable cause)
    {
        super(cause);
    }

    public DatabaseIntegrityException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
