import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object ColdSerializerInitialization {
    @JvmStatic
    fun main(args: Array<String>) {
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(8)
        try {
            val futures =
                (0 until 32).map { index ->
                    executor.submit<String> {
                        start.await()
                        when (index % 4) {
                            0 ->
                                skirout.structs.RecA.serializer.toJsonCode(
                                    skirout.structs.RecA.partial(),
                                )
                            1 -> skirout.structs.RecB.typeDescriptor.qualifiedName
                            2 -> skirout.enums.Status.typeDescriptor.qualifiedName
                            else -> skirout.structs.RecB.partial().toString()
                        }
                    }
                }
            start.countDown()
            val results = futures.map { it.get(10, TimeUnit.SECONDS) }
            check(results.all(String::isNotEmpty))
        } finally {
            executor.shutdownNow()
        }
    }
}
