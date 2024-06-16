package org.babyfish.jimmer.sql.kt.model.link

import org.babyfish.jimmer.sql.*

@Entity
interface Student {

    @Id
    val id: Long

    val name: String

    @OneToMany(mappedBy = "student")
    val learningLinks: List<LearningLink>

    @ManyToManyView(prop = "learningLinks")
    val courses: List<Course>

    @LogicalDeleted("true")
    val deleted: Boolean
}