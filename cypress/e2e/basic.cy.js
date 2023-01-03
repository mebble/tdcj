/// <reference types="cypress" />

describe('basics', () => {
    const existingTodos = [
        'Feed the cat',
        'Do dishes',
    ];

    beforeEach(() => {
        cy.visit('/')
        cy.get('#new-todo-txt').type(existingTodos[0])
        cy.get('#new-todo-btn').click()
        cy.get('#new-todo-txt').type(existingTodos[1])
        cy.get('#new-todo-btn').click()
        cy.get('#todo-list li').should('have.length', 2)
    })

    it('adds and deletes a todo', () => {
        const todo = 'Pay bills'
        cy.get('#new-todo-txt').type(todo)
        cy.get('#new-todo-btn').click()
        cy.get('#todo-list li')
            .should('have.length', 3)
            .last()
            .should('have.text', todo)

        cy.get('[data-delete=1]').click()
        cy.get('#todo-list li')
            .should('have.length', 2)
            .first()
            .should('have.text', existingTodos[1])
    })

    it('checks off a todo', () => {
        cy.get('[data-done=1]').click()
        cy.get('#todo-list li')
            .should('have.length', 2)
            .first()
            .should('have.attr', 'data-is-done', "true")
    })

    it('edits a todo', () => {
        const newText = 'Feed the dog';

        cy.get('[data-edit=1]').click()
            .should('be.checked')
            .parent()
            .should('not.have.text', existingTodos[0])

        cy.get('[data-input=1]')
            .should('have.value', existingTodos[0])
            .type('{backspace}{backspace}{backspace}dog')

        cy.get('[data-input=1]')
            .should('have.value', newText)

        cy.get('[data-edit=1]').click()
            .should('not.be.checked')
            .parent()
            .should('have.text', newText)

        cy.get('[data-input=1]')
            .should('not.exist')
    })
})
