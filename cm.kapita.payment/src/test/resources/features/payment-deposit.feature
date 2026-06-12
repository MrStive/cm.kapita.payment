Feature: Payment Deposit

  Scenario: Successful deposit via Monetbil
    Given I initiate a payment with following data
      | amount | currency | phoneNumber | provider |
      | 1000   | XAF      | 699000000   | MONETBIL |
    When I receive a success notification from Monetbil
    Then the transaction status should be COMPLETED
    And a PaymentStatusEvent should be emitted to Kafka with status "SUCCESS"
