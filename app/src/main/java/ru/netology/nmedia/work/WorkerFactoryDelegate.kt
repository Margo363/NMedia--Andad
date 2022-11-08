package ru.netology.nmedia.work

import androidx.work.DelegatingWorkerFactory
import ru.netology.nmedia.repository.PostRepository


class WorkerFactoryDelegate( repository: PostRepository) : DelegatingWorkerFactory() {
    init {
        addFactory(RefreshPostsWorkerFactory(repository))
        addFactory(RemovePostWorkerFactory(repository))
        addFactory(SavePostWorkerFactory(repository))
    }
}