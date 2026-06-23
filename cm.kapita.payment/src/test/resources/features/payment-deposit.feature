#@e2e
#Feature: Payment Deposit
#
#  Scenario: Successful deposit via Monetbil
#    Given I initiate a payment with following data
#      | externalReference | purpose              | amount | currency | phoneNumber | provider | idempotencyKey | description |
#      | sub-order-1       | SUBSCRIPTION_PAYMENT | 1000   | XAF      | 699000000   | MONETBIL | test-id-1      | Subscription payment |
#    When I receive a success notification from Monetbil
#    Then the transaction status should be "SUCCEEDED"
#    And a PaymentStatusEvent should be emitted to Kafka with status "SUCCEEDED"
