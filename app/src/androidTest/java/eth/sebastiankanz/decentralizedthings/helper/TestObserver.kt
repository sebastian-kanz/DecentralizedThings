package eth.sebastiankanz.decentralizedthings.helper

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import org.awaitility.Awaitility
import org.awaitility.Durations
import org.junit.Assert

/**
 * An [Observer] subclass that captures emitted values of a [LiveData] to [observedValues].
 *
 * Make sure to use:
 * [@get:Rule
 * val rule: TestRule = InstantTaskExecutorRule()]
 *
 * @author MaibornWolff
 */
class TestObserver<T> : Observer<T> {

    val observedValues = mutableListOf<T?>()

    override fun onChanged(value: T?) {
        observedValues.add(value)
    }

    /**
     * Assert a specific number of items emitted.
     *
     * @return The instance of this [TestObserver].
     */
    fun assertValueCount(expectedValuesCount: Int): TestObserver<T> {
        if (expectedValuesCount != observedValues.size)
            throw AssertionError("Expected value count: $expectedValuesCount\nActual count: ${observedValues.size}")

        return this
    }

    /**
     * Assert that only one specific value was emitted by the observed LiveData.
     *
     * @return The instance of this [TestObserver].
     */
    fun assertValue(expected: T?): TestObserver<T> {
        if (observedValues.isEmpty())
            throw AssertionError("There was one expected value, but no values have been emitted.")
        else if (observedValues.size > 1)
            throw AssertionError("There was only one expected value, but ${observedValues.size} have been emitted.")

        val actualValue: T? = observedValues.first()
        if (actualValue != expected)
            throw AssertionError(
                "The expected and emitted values are different!\n" +
                    "Expected: ${expected.toString()}\n" +
                    "Actual: ${actualValue.toString()}"
            )

        return this
    }

    /**
     * Assert that only one value was emitted by the observed LiveData and
     * use it as parameter for the given action.
     *
     * @return The instance of this [TestObserver].
     */
    inline fun assertValue(action: (T?) -> Unit): TestObserver<T> {
        if (observedValues.isEmpty())
            throw AssertionError("There was one expected value, but no values have been emitted.")
        else if (observedValues.size > 1)
            throw AssertionError("There was only one expected value, but ${observedValues.size} have been emitted.")

        action.invoke(observedValues.first())

        return this
    }

    /**
     * Assert that the last value emitted by the observed LiveData matches a specific value.
     *
     * @return The instance of this [TestObserver].
     */
    fun assertLastValue(expected: T?): TestObserver<T> {
        if (observedValues.isEmpty())
            throw AssertionError("There was one expected value, but no values have been emitted.")

        val actualValue: T? = observedValues.last()
        if (actualValue != expected)
            throw AssertionError(
                "The expected and emitted values are different!\n" +
                    "Expected: ${expected.toString()}\n" +
                    "Actual: ${actualValue.toString()}"
            )

        return this
    }

    /**
     * Assert that at least one value was emitted by the observed LiveData and
     * use the last emitted value as parameter for the given action.
     *
     * @return The instance of this [TestObserver].
     */
    inline fun assertLastValue(action: (T?) -> Unit): TestObserver<T> {
        if (observedValues.isEmpty())
            throw AssertionError("There was one expected value, but no values have been emitted.")

        action.invoke(observedValues.last())

        return this
    }

    /**
     * Asserts that the specific values were emitted by the observed LiveData in the provided order.
     *
     * @return The instance of this [TestObserver].
     */
    fun assertValues(vararg expected: T?): TestObserver<T> {
        if (observedValues.isEmpty())
            throw AssertionError("There were ${expected.size} expected values, but no values have been emitted.")
        else if (expected.size != observedValues.size)
            throw AssertionError("Expected value count: ${expected.size}\nActual count: ${observedValues.size}")

        expected.forEachIndexed { index, expectedValue ->
            val actualValue = observedValues[index]
            if (actualValue != expectedValue)
                throw AssertionError(
                    "The expected and emitted values are different!\n" +
                        "Expected: ${expectedValue.toString()}\n" +
                        "Actual: ${actualValue.toString()}"
                )
        }

        return this
    }

    /**
     * Asserts that the expected last values is emitted by observed LiveData
     *
     * @return The instance of this [TestObserver].
     */

    fun assertLastValueWait(expected: T?): TestObserver<T> {
        Awaitility.await().atMost(Durations.TEN_SECONDS).untilAsserted { Assert.assertEquals(expected, this.observedValues.last()) }
        return this
    }
}
