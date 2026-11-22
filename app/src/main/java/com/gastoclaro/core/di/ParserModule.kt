package com.gastoclaro.core.di

import com.gastoclaro.domain.parser.BancolombiaParser
import com.gastoclaro.domain.parser.BbvaParser
import com.gastoclaro.domain.parser.DaviplataParser
import com.gastoclaro.domain.parser.DaviviendaParser
import com.gastoclaro.domain.parser.NequiParser
import com.gastoclaro.domain.parser.NuParser
import com.gastoclaro.domain.parser.TransactionParser
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class ParserModule {

    @Binds
    @IntoSet
    abstract fun bindBancolombia(parser: BancolombiaParser): TransactionParser

    @Binds
    @IntoSet
    abstract fun bindNequi(parser: NequiParser): TransactionParser

    @Binds
    @IntoSet
    abstract fun bindDaviplata(parser: DaviplataParser): TransactionParser

    @Binds
    @IntoSet
    abstract fun bindDavivienda(parser: DaviviendaParser): TransactionParser

    @Binds
    @IntoSet
    abstract fun bindBbva(parser: BbvaParser): TransactionParser

    @Binds
    @IntoSet
    abstract fun bindNu(parser: NuParser): TransactionParser
}
