package com.warmerhammer.personalnotes.SearchActivity

class UserTrie {

    class TrieNode {
        val children = Array<TrieNode?>(26) { null }
        var isWord = false
    }

    val trieTree = TrieNode()

    fun insert(word: String) {
        var p = trieTree

        for (w in word) {
            val i = w - 'a'
            if (p.children[i] == null) {
                p.children[i] = TrieNode()
            }
            p = p.children[i]!!
        }

        p.isWord = true
    }

    fun search(word: String): Boolean {
        var p = trieTree

        for (w in word) {
            val i = w - 'a'
            if (p.children[i] == null) return false
            p = p.children[i]!!
        }

        return p.isWord
    }

    fun startsWith(prefix: String): Boolean {
        var p = trieTree

        for (w in prefix) {
            val i = w - 'a'
            if (p.children[i] == null) return false
            p = p.children[i]!!
        }

        return true
    }

}