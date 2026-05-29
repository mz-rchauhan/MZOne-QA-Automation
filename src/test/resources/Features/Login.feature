Feature: Case Creation flows


  Scenario: ES - Other Query flow
    Given Login as an System admin
    When Log in as "Employee" on the Lightning site
    And Click on the "My Requests" link
    And Click on the "New Support Ticket" link
    And Click on the "ES - Other Query" link
    And Fill the below details :
      |Data Type|Field Name|Value|Field Label|
      |Text|Subject|Test Subject|Subject|
      |Text|Description|Test Description|Description|
    And Click on "Next" button
    And Upload file in "Do you want to upload any reference documents here?"
    And Click on "Next" button
    Then Store the "Case Number" from confirmation message as "Your case #"
    And Switch to the 1 tab opened
    And I log out as user
    And Login as an System admin
    And Log in as user with user name as "ES_agent"
    And Search for the "Cases" object with name "Test Context:Case Number"
    And Validate the record path status should be "New"
    And Click on "Accept" button
    And Validate the record path status should be "Open"
    And Click on "Close Case" button
    And Fill the below details :
      |Data Type|Field Name|Value|Field Label|
      |Dropdown Backend|Resolution Type|Case Resolved|Resolution Type|
      |Textarea|Resolution Note|Test Resolution Note|Resolution Note|
    And Click on "Save" button
    And Validate the record path status should be "Closed"
    Then Validate following fields:
      | Field Name | Value |
      | Description | Test Description |
      | Resolution Type | FromTextContext |
      | Resolution Note | FromTextContext |



