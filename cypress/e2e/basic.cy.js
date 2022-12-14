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
            .should(() => {
                const expectedMeta = '["todo:1" "todo:2" "todo:3"]'
                expect(localStorage.getItem('meta:ids')).to.eq(expectedMeta)
                expect(localStorage.getItem('todo:1')).to.not.be.null
                expect(localStorage.getItem('todo:2')).to.not.be.null
                expect(localStorage.getItem('todo:3')).to.not.be.null
            })

        cy.get('[data-delete=1]').click()
        cy.get('#todo-list li')
            .should('have.length', 2)
            .first()
            .should('have.text', existingTodos[1])
            .should(() => {
                const expectedMeta = '["todo:2" "todo:3"]'
                expect(localStorage.getItem('meta:ids')).to.eq(expectedMeta)
                expect(localStorage.getItem('todo:1')).to.be.null
                expect(localStorage.getItem('todo:2')).to.not.be.null
                expect(localStorage.getItem('todo:3')).to.not.be.null
            })
    })

    it('checks off a todo', () => {
        cy.get('[data-done=1]').click()
        cy.get('#todo-list li')
            .should('have.length', 2)
            .first()
            .should('have.attr', 'data-is-done', "true")
            .should(() => {
                const expectedMeta = '["todo:1" "todo:2"]'
                expect(localStorage.getItem('meta:ids')).to.eq(expectedMeta)
                expect(localStorage.getItem('todo:1')).to.contain(':done true')
                expect(localStorage.getItem('todo:2')).to.contain(':done false')
            })

        cy.get('[data-done=1]').click()
        cy.get('#todo-list li')
            .should('have.length', 2)
            .first()
            .should('have.attr', 'data-is-done', "false")
            .should(() => {
                const expectedMeta = '["todo:1" "todo:2"]'
                expect(localStorage.getItem('meta:ids')).to.eq(expectedMeta)
                expect(localStorage.getItem('todo:1')).to.contain(':done false')
                expect(localStorage.getItem('todo:2')).to.contain(':done false')
            })
    })

    it('edits a todo', () => {
        const oldText = existingTodos[0];
        const newText = 'Feed the dog';

        cy.get('[data-edit=1]').click()
            .should('be.checked')
            .parent()
            .should('not.have.text', oldText)

        cy.get('[data-input=1]')
            .should('have.value', oldText)
            .type('{backspace}{backspace}{backspace}dog')

        cy.get('[data-input=1]')
            .should('have.value', newText)
            .should(() => {
                const expectedMeta = '["todo:1" "todo:2"]'
                expect(localStorage.getItem('meta:ids')).to.eq(expectedMeta)
                expect(localStorage.getItem('todo:1')).to.contain(`:txt "${oldText}"`)
            })

        cy.get('[data-edit=1]').click()
            .should('not.be.checked')
            .parent()
            .should('have.text', newText)
            .should(() => {
                const expectedMeta = '["todo:1" "todo:2"]'
                expect(localStorage.getItem('meta:ids')).to.eq(expectedMeta)
                expect(localStorage.getItem('todo:1')).to.contain(`:txt "${newText}"`)
            })

        cy.get('[data-input=1]')
            .should('not.exist')
    })
})
