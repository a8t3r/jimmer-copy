package org.babyfish.jimmer.example.kt.sql.model

import org.babyfish.jimmer.sql.*

@Entity
interface TreeNode {
    
    @Id
    @Column(name = "NODE_ID")
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        sequenceName = "TREE_NODE_ID_SEQ"
    )
    val id: Long

    @Key
    val name: String

    @Key
    @ManyToOne
    @OnDelete(DeleteAction.CASCADE)
    val parent: TreeNode?

    @OneToMany(mappedBy = "parent")
    val childNodes: List<TreeNode>
}