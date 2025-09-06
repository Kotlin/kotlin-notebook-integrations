package org.jetbrains.kotlinx.jupyter.database

import javax.sql.DataSource

interface DataSourceStub {
    fun createDataSource(): DataSource
}