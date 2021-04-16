package demo.undertow

import io.undertow.security.idm.Account
import io.undertow.security.idm.Credential
import io.undertow.security.idm.IdentityManager
import io.undertow.security.idm.PasswordCredential
import java.security.Principal
import java.util.*


internal class MapIdentityManager(private val users: Map<String, CharArray>) : IdentityManager {
    override fun verify(account: Account): Account {
        // An existing account so for testing assume still valid.
        return account
    }

    override fun verify(id: String, credential: Credential): Account? {
        val account = getAccount(id)
        return if (account != null && verifyCredential(account, credential)) {
            account
        } else null
    }

    override fun verify(credential: Credential): Account? {
        // TODO Auto-generated method stub
        return null
    }

    private fun verifyCredential(account: Account, credential: Credential): Boolean {
        if (credential is PasswordCredential) {
            val password = credential.password
            val expectedPassword = users[account.principal.name]
            return Arrays.equals(password, expectedPassword)
        }
        return false
    }

    private fun getAccount(id: String): Account? {
        return if (users.containsKey(id)) {
            object : Account {
                private val principal = Principal { id }

                override fun getPrincipal(): Principal {
                    return principal
                }

                override fun getRoles(): Set<String> {
                    return emptySet()
                }
            }
        } else null
    }
}