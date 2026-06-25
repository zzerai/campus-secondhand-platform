package com.ruoyi.common.storage;

/**
 * 存储层异常
 *
 * @author trading
 */
public class StorageException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public StorageException(String message)
    {
        super(message);
    }

    public StorageException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
