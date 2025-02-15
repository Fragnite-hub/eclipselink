/*
 * Copyright (c) 1998, 2022 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998, 2022 IBM Corporation. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

// Contributors:
//     Oracle - initial API and implementation from Oracle TopLink
//     02/03/2017 - Dalia Abo Sheasha
//       - 509693 : EclipseLink generates inconsistent SQL statements for SubQuery

package org.eclipse.persistence.testing.tests.jpa.criteria.advanced;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Parameter;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.ParameterExpression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.metamodel.EmbeddableType;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.persistence.config.CacheUsage;
import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.config.QueryType;
import org.eclipse.persistence.config.ResultSetConcurrency;
import org.eclipse.persistence.config.ResultSetType;
import org.eclipse.persistence.config.ResultType;
import org.eclipse.persistence.internal.jpa.querydef.CompoundExpressionImpl;
import org.eclipse.persistence.internal.jpa.querydef.CriteriaQueryImpl;
import org.eclipse.persistence.internal.jpa.querydef.FromImpl;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.jpa.JpaCriteriaBuilder;
import org.eclipse.persistence.jpa.JpaQuery;
import org.eclipse.persistence.queries.Cursor;
import org.eclipse.persistence.queries.ScrollableCursor;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.testing.framework.QuerySQLTracker;
import org.eclipse.persistence.testing.framework.jpa.junit.JUnitTestCase;
import org.eclipse.persistence.testing.models.jpa.advanced.Address;
import org.eclipse.persistence.testing.models.jpa.advanced.AdvancedTableCreator;
import org.eclipse.persistence.testing.models.jpa.advanced.Dealer;
import org.eclipse.persistence.testing.models.jpa.advanced.Employee;
import org.eclipse.persistence.testing.models.jpa.advanced.EmployeePopulator;
import org.eclipse.persistence.testing.models.jpa.advanced.EmploymentPeriod;
import org.eclipse.persistence.testing.models.jpa.advanced.PhoneNumber;
import org.eclipse.persistence.testing.models.jpa.advanced.Project;
import org.eclipse.persistence.testing.tests.jpa.criteria.JUnitDomainObjectComparer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * <p>
 * <b>Purpose</b>: Test advanced JPA Query functionality.
 * <p>
 * <b>Description</b>: This tests query hints, caching and query optimization.
 *
 */
public class AdvancedCriteriaQueryTest extends JUnitTestCase {

    static JUnitDomainObjectComparer comparer; //the global comparer object used in all tests

    public AdvancedCriteriaQueryTest() {
        super();
    }

    public AdvancedCriteriaQueryTest(String name) {
        super(name);
        setPuName(getPersistenceUnitName());
    }

    @Override
    public String getPersistenceUnitName() {
        return "advanced";
    }

    // This method is run at the start of EVERY test case method.

    @Override
    public void setUp() {

    }

    // This method is run at the end of EVERY test case method.

    @Override
    public void tearDown() {
        clearCache();
    }

    //This suite contains all tests contained in this class

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.setName("AdvancedCriteriaQueryTest");
        suite.addTest(new AdvancedCriteriaQueryTest("testSetup"));
        suite.addTest(new AdvancedCriteriaQueryTest("testInCollectionEntity"));
        suite.addTest(new AdvancedCriteriaQueryTest("testInCollectionPrimitives"));
        suite.addTest(new AdvancedCriteriaQueryTest("testInParameterCollection"));
        suite.addTest(new AdvancedCriteriaQueryTest("testInParameterCollection2"));
        suite.addTest(new AdvancedCriteriaQueryTest("testProd"));
        suite.addTest(new AdvancedCriteriaQueryTest("testSize"));
        suite.addTest(new AdvancedCriteriaQueryTest("testJoinDistinct"));
        suite.addTest(new AdvancedCriteriaQueryTest("testSome"));
        suite.addTest(new AdvancedCriteriaQueryTest("testWhereConjunction"));
        suite.addTest(new AdvancedCriteriaQueryTest("testWhereNotConjunction"));
        suite.addTest(new AdvancedCriteriaQueryTest("testWhereDisjunction"));
        suite.addTest(new AdvancedCriteriaQueryTest("testWhereNotDisjunction"));
        suite.addTest(new AdvancedCriteriaQueryTest("testWhereConjunctionAndDisjunction"));
        suite.addTest(new AdvancedCriteriaQueryTest("testWhereDisjunctionAndConjunction"));
        suite.addTest(new AdvancedCriteriaQueryTest("testWhereConjunctionOrDisjunction"));
        suite.addTest(new AdvancedCriteriaQueryTest("testWhereUsingAndWithPredicates"));
        suite.addTest(new AdvancedCriteriaQueryTest("testWhereUsingOrWithPredicates"));
        suite.addTest(new AdvancedCriteriaQueryTest("testVerySimpleJoin"));
        suite.addTest(new AdvancedCriteriaQueryTest("testGroupByHaving"));
        suite.addTest(new AdvancedCriteriaQueryTest("testGroupByHaving2"));
        suite.addTest(new AdvancedCriteriaQueryTest("testAlternateSelection"));
        suite.addTest(new AdvancedCriteriaQueryTest("testSubqueryExists"));
        suite.addTest(new AdvancedCriteriaQueryTest("testSubqueryNotExists"));
        suite.addTest(new AdvancedCriteriaQueryTest("testSubqueryExistsAfterAnd"));
        suite.addTest(new AdvancedCriteriaQueryTest("testSubqueryExistsBeforeAnd"));
        suite.addTest(new AdvancedCriteriaQueryTest("testSubqueryNotExistsAfterAnd"));
        suite.addTest(new AdvancedCriteriaQueryTest("testSubqueryNotExistsBeforeAnd"));
        suite.addTest(new AdvancedCriteriaQueryTest("testSubqueryExistsNested"));
        suite.addTest(new AdvancedCriteriaQueryTest("testSubqueryExistsNestedUnusedRoot"));
        suite.addTest(new AdvancedCriteriaQueryTest("testSubqueryExistsNestedAfterAnd"));
        suite.addTest(new AdvancedCriteriaQueryTest("testSubqueryExistsNestedAfterLiteralAnd"));
        suite.addTest(new AdvancedCriteriaQueryTest("testSubQuery"));
        suite.addTest(new AdvancedCriteriaQueryTest("testInSubQuery"));
        suite.addTest(new AdvancedCriteriaQueryTest("testInLiteral"));
        suite.addTest(new AdvancedCriteriaQueryTest("testInlineInParameter"));
        suite.addTest(new AdvancedCriteriaQueryTest("testSimpleJoin"));
        suite.addTest(new AdvancedCriteriaQueryTest("testSimpleFetch"));
        suite.addTest(new AdvancedCriteriaQueryTest("testObjectResultType"));
        suite.addTest(new AdvancedCriteriaQueryTest("testSimple"));
        suite.addTest(new AdvancedCriteriaQueryTest("testSimpleWhere"));
        suite.addTest(new AdvancedCriteriaQueryTest("testSimpleWhereObject"));
        suite.addTest(new AdvancedCriteriaQueryTest("testSharedWhere"));
        suite.addTest(new AdvancedCriteriaQueryTest("testTupleQuery"));
        suite.addTest(new AdvancedCriteriaQueryTest("testQueryCacheFirstCacheHits"));
        suite.addTest(new AdvancedCriteriaQueryTest("testQueryCacheOnlyCacheHits"));
        suite.addTest(new AdvancedCriteriaQueryTest("testQueryCacheOnlyCacheHitsOnSession"));
        suite.addTest(new AdvancedCriteriaQueryTest("testQueryExactPrimaryKeyCacheHits"));
        suite.addTest(new AdvancedCriteriaQueryTest("testQueryHintFetch"));
        suite.addTest(new AdvancedCriteriaQueryTest("testCursors"));
        suite.addTest(new AdvancedCriteriaQueryTest("testIsEmpty"));
        suite.addTest(new AdvancedCriteriaQueryTest("testNeg"));
        suite.addTest(new AdvancedCriteriaQueryTest("testIsMember"));
        suite.addTest(new AdvancedCriteriaQueryTest("testIsMemberEntity"));
        suite.addTest(new AdvancedCriteriaQueryTest("testNullRestrictionGetRestriction"));
        suite.addTest(new AdvancedCriteriaQueryTest("testFromToExpression"));
        suite.addTest(new AdvancedCriteriaQueryTest("testUnusedJoinDoesNotAffectOtherJoins"));
        suite.addTest(new AdvancedCriteriaQueryTest("testUnusedJoinDoesNotAffectFetchJoin"));
        // Bug 464833
        suite.addTest(new AdvancedCriteriaQueryTest("testGetRestrictionReturningCorrectPredicate"));
        suite.addTest(new AdvancedCriteriaQueryTest("testJoinDuplication"));

        return suite;
    }

    /**
     * The setup is done as a test, both to record its failure, and to allow execution in the server.
     */
    public void testSetup() {
        clearCache();
        DatabaseSession session = getPersistenceUnitServerSession();
        //create a new EmployeePopulator
        EmployeePopulator employeePopulator = new EmployeePopulator(supportsStoredProcedures());
        new AdvancedTableCreator().replaceTables(session);
        //initialize the global comparer object
        comparer = new JUnitDomainObjectComparer();
        //set the session for the comparer to use
        comparer.setSession((AbstractSession)session.getActiveSession());
        //Populate the tables
        employeePopulator.buildExamples();
        //Persist the examples in the database
        employeePopulator.persistExample(session);
    }



    public void testAlternateSelection() {
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try {
            em.createQuery("select p.teamLeader from Project p where p.name = 'Sales Reporting'").getResultList();
            Metamodel mm = em.getMetamodel();
            CriteriaBuilder qbuilder = em.getCriteriaBuilder();
            CriteriaQuery<Employee> cquery = qbuilder.createQuery(Employee.class);
            Root<Project> spouse = cquery.from(Project.class);
            cquery.where(qbuilder.equal(spouse.get("name"), "Sales Reporting")).select(spouse.get("teamLeader"));
            TypedQuery<Employee> tquery = em.createQuery(cquery);
            assertTrue("Did not find the correct leaders of Project Swirly Dirly.", tquery.getResultList().size() > 1);
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    /**
     * Test that a cache hit will occur on a primary key query.
     */
    public void testTupleQuery() {
        EntityManager em = createEntityManager();
        QuerySQLTracker counter = null;
        beginTransaction(em);
        try {
            // Load an employee into the cache.
            CriteriaBuilder qb = em.getCriteriaBuilder();

            TypedQuery<Employee> query = em.createQuery(em.getCriteriaBuilder().createQuery(Employee.class));
            List<Employee> result = query.getResultList();
            Employee employee = result.get(0);

            // Count SQL.
            counter = new QuerySQLTracker(getPersistenceUnitServerSession());
            // Query by primary key.
            CriteriaQuery<Tuple> cq = qb.createQuery(Tuple.class);
            Root<Employee> from = cq.from(Employee.class);
            cq.multiselect(from.get("id"), from.get("firstName"));
            cq.where(qb.and(qb.equal(from.get("id"), qb.parameter(from.get("id").getModel().getBindableJavaType(), "id")), qb.equal(from.get("firstName"), qb.parameter(from.get("firstName").getModel().getBindableJavaType(), "firstName"))));
            TypedQuery<Tuple> typedQuery = em.createQuery(cq);

            typedQuery.setParameter("id", employee.getId());
            typedQuery.setParameter("firstName", employee.getFirstName());

            Tuple queryResult = typedQuery.getSingleResult();
            assertTrue("Query Results do not match selection", queryResult.get(0).equals(employee.getId()) && queryResult.get(1).equals(employee.getFirstName()));
        } finally {
            rollbackTransaction(em);
            if (counter != null) {
                counter.remove();
            }
        }
    }

    public void testSharedWhere() {
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try{
            CriteriaQuery<Employee> cq = em.getCriteriaBuilder().createQuery(Employee.class);
            CriteriaBuilder qb = em.getCriteriaBuilder();
            Root<Employee> root = cq.from(em.getMetamodel().entity(Employee.class));

            cq.where(qb.equal(root.get("firstName"), qb.literal("Bob")));

            TypedQuery<Employee> tq = em.createQuery(cq);
            List<Employee> result = tq.getResultList();
            assertFalse("No Employees were returned", result.isEmpty());
            assertEquals("Did not return Employee", result.get(0).getClass(), Employee.class);
            assertTrue("Employee had wrong firstname", result.get(0).getFirstName().equalsIgnoreCase("bob"));

            CriteriaQuery<Employee> cq2 = em.getCriteriaBuilder().createQuery(Employee.class);
            cq2.where(cq.getRestriction());
            TypedQuery<Employee> tq2 = em.createQuery(cq);
            List<Employee> result2 = tq.getResultList();
            assertTrue("Employee's did not match with query with same where clause", comparer.compareObjects(result.get(0), result2.get(0)));
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    public void testSimple(){
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try {
            CriteriaQuery<Employee> cq = em.getCriteriaBuilder().createQuery(Employee.class);
            List<Employee> result = em.createQuery(cq).getResultList();
            assertFalse("No Employees were returned", result.isEmpty());
            assertEquals("Did not return Employee", result.get(0).getClass(), Employee.class);
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    public void testGroupByHaving(){
        EntityManager em = createEntityManager();

        List<?> jpqlResult = em.createQuery("Select e.address, count(e) from Employee e group by e.address having count(e.address) < 3").getResultList();
        beginTransaction(em);
        try {
            Metamodel mm = em.getMetamodel();
            CriteriaBuilder qbuilder = em.getCriteriaBuilder();
            CriteriaQuery<Object> cquery = qbuilder.createQuery();
            Root<Employee> customer = cquery.from(Employee.class);
            cquery.multiselect(customer.get("address"), qbuilder.count(customer));
            cquery.groupBy(customer.get("address"));
            cquery.having(qbuilder.lt(qbuilder.count(customer.get("address")), 3));
            List<Object> result = em.createQuery(cquery).getResultList();
            assertTrue(comparer.compareObjects(jpqlResult, result));
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    public void testGroupByHaving2(){
        EntityManager em = createEntityManager();

        List<?> jpqlResult = em.createQuery("Select e.period, count(e) from Employee e group by e.period having count(e.period) > 3").getResultList();
        beginTransaction(em);
        try {
            Metamodel mm = em.getMetamodel();
            CriteriaBuilder qbuilder = em.getCriteriaBuilder();
            CriteriaQuery<?> cquery = qbuilder.createQuery();
            Root<Employee> customer = cquery.from(Employee.class);
            EntityType<Employee> Customer_ = customer.getModel();
            EmbeddableType<EmploymentPeriod> Country_ = mm.embeddable(EmploymentPeriod.class);
            cquery.multiselect(customer.get(Customer_.getSingularAttribute("period", EmploymentPeriod.class)), qbuilder.count(customer));
            cquery.groupBy(customer.get(Customer_.getSingularAttribute("period", EmploymentPeriod.class)));
            cquery.having(qbuilder.gt(qbuilder.count(customer.get(Customer_.getSingularAttribute("period", EmploymentPeriod.class))), 3));
            List<?> result = em.createQuery(cquery).getResultList();
            assertTrue(comparer.compareObjects(jpqlResult, result));
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    public void testInLiteral(){
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try {
            CriteriaBuilder qb = em.getCriteriaBuilder();
            CriteriaQuery<Employee> cq = qb.createQuery(Employee.class);
            Root<Employee> emp = cq.from(Employee.class);
            In<String> in = qb.in(emp.get("address").get("city"));
            in.value("Ottawa").value("Halifax").value("Toronto");
            cq.where(in);
            List<Employee> result = em.createQuery(cq).getResultList();
            assertFalse("No Employees were returned", result.isEmpty());
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    public void testInSubQuery(){
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try {
            CriteriaBuilder qb = em.getCriteriaBuilder();
            CriteriaQuery<Employee> cq = qb.createQuery(Employee.class);
            Root<Employee> emp = cq.from(Employee.class);
            Subquery<String> sq = cq.subquery(String.class);
            Root<Address> sqEmp = sq.from(Address.class);
            sq.select(sqEmp.get("city"));
            sq.where(qb.notLike(sqEmp.get("city"), "5"));
            In<String> in = qb.in(emp.get("address").get("city"));
            in.value(sq);
            cq.where(in);
            List<Employee> result = em.createQuery(cq).getResultList();
            assertFalse("No Employees were returned", result.isEmpty());
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    public void testInCollectionEntity(){
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try {
            CriteriaBuilder qb = em.getCriteriaBuilder();
            CriteriaQuery<Employee> cq = qb.createQuery(Employee.class);
            Root<Employee> emp = cq.from(Employee.class);
            Root<PhoneNumber> phone = cq.from(PhoneNumber.class);
            cq.where(qb.and(qb.equal(phone.get("areaCode"), "613"), phone.in(emp.get("phoneNumbers"))));
            TypedQuery<Employee> query = em.createQuery(cq);
            List<Employee> result = query.getResultList();
            assertFalse("No Employees were returned", result.isEmpty());
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    public void testInCollectionPrimitives(){
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try {
            CriteriaBuilder qb = em.getCriteriaBuilder();
            CriteriaQuery<Employee> cq = qb.createQuery(Employee.class);
            Root<Employee> emp = cq.from(Employee.class);
            Root<PhoneNumber> phone = cq.from(PhoneNumber.class);
            cq.where(qb.literal("Bug fixes").in(emp.get("responsibilities")));
            TypedQuery<Employee> query = em.createQuery(cq);
            List<Employee> result = query.getResultList();
            assertFalse("No Employees were returned", result.isEmpty());
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    public void testInCollectionEmpty(){
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try {
            CriteriaBuilder qb = em.getCriteriaBuilder();
            CriteriaQuery<Employee> cq = qb.createQuery(Employee.class);
            Root<Employee> emp = cq.from(Employee.class);
            Root<PhoneNumber> phone = cq.from(PhoneNumber.class);
            cq.where(qb.literal("Bug fixes").in(new HashSet<>()));
            TypedQuery<Employee> query = em.createQuery(cq);
            List<Employee> result = query.getResultList();
            assertTrue("No any Employees was expected", result.isEmpty());
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    public void testInCollectionNull(){
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try {
            CriteriaBuilder qb = em.getCriteriaBuilder();
            CriteriaQuery<Employee> cq = qb.createQuery(Employee.class);
            Root<Employee> emp = cq.from(Employee.class);
            Root<PhoneNumber> phone = cq.from(PhoneNumber.class);
            List<String> list = null;
            cq.where(qb.literal("Bug fixes").in(list));
            TypedQuery<Employee> query = em.createQuery(cq);
            List<Employee> result = query.getResultList();
            assertTrue("No any Employees was expected", result.isEmpty());
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    /*
     * bug 349477 - Using criteria.in(...) with ParameterExpression of type Collection creates invalid SQL
     */
    public void testInParameterCollection(){
        EntityManager em = createEntityManager();
        beginTransaction(em);
        List<String> respons = new Vector<>();
        respons.add("NoResults");
        respons.add("Bug fixes");
        try {
            CriteriaBuilder qbuilder = em.getCriteriaBuilder();
            CriteriaQuery<Employee> cquery =qbuilder.createQuery(Employee.class);
            Root<Employee> emp = cquery.from(Employee.class);
            ParameterExpression<Collection> pe = qbuilder.parameter(java.util.Collection.class, "param");
            cquery.where(emp.join("responsibilities").in(pe));
            List<Employee> result = em.createQuery(cquery).setParameter("param",respons).getResultList();
            assertFalse("testInParameterCollection failed: No Employees were returned", result.isEmpty());
        } finally {
            closeEntityManagerAndTransaction(em);
        }
    }

    /*
     * bug 349477 - Using criteria.in(...) with ParameterExpression of type Collection creates invalid SQL
     */
    public void testInParameterCollection2(){
        EntityManager em = createEntityManager();
        beginTransaction(em);
        List<String> response = new Vector<>();
        response.add("NoResults");
        response.add("Bug fixes");
        try {
            CriteriaBuilder qbuilder = em.getCriteriaBuilder();
            CriteriaQuery<Employee> cquery = qbuilder.createQuery(Employee.class);
            Root<Employee> emp = cquery.from(Employee.class);
            cquery.where(emp.join("responsibilities").in(qbuilder.parameter(java.util.Collection.class, "param")));
            List<Employee> result = em.createQuery(cquery).setParameter("param", response).getResultList();
            assertFalse("testInParameterCollection failed: No Employees were returned", result.isEmpty());
        } finally {
            closeEntityManagerAndTransaction(em);
        }
    }

    public void testInlineInParameter(){
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try {
            CriteriaBuilder qb = em.getCriteriaBuilder();
            CriteriaQuery<Employee> cq = qb.createQuery(Employee.class);
            Root<Employee> emp = cq.from(Employee.class);
            cq.where(emp.get("address").<String>get("city").in(qb.parameter(String.class, "city")));
            TypedQuery<Employee> query = em.createQuery(cq);
            query.setParameter("city", "Ottawa");
            List<Employee> result = query.getResultList();
            assertFalse("No Employees were returned", result.isEmpty());
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    public void testIsEmpty(){
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try{
            CriteriaBuilder qb = em.getCriteriaBuilder();
            CriteriaQuery<Employee> cq = qb.createQuery(Employee.class);
            Root<Employee> emp = cq.from(Employee.class);
            cq.where(qb.isEmpty(emp.<Collection<PhoneNumber>>get("phoneNumbers")));
            List<Employee> result = em.createQuery(cq).getResultList();
            assertFalse("No Employees were returned", result.isEmpty());
            for (Employee e : result){
                assertTrue("PhoneNumbers Found", e.getPhoneNumbers().isEmpty());
            }
        }finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    public void testNeg(){
        if (getPlatform().isSymfoware()) {
            getPersistenceUnitServerSession().logMessage("Test testNeg skipped for this platform, "
                    + "Symfoware doesn't allow arithmetic expression in subquery. (bug 304907)");
            return;
        }
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try{
            CriteriaBuilder qb = em.getCriteriaBuilder();
            CriteriaQuery<Employee> cq = qb.createQuery(Employee.class);
            Root<Employee> emp = cq.from(Employee.class);
            cq.where(qb.lessThan(qb.neg(qb.size(emp.<Collection<PhoneNumber>>get("phoneNumbers"))), 0));
            List<Employee> result = em.createQuery(cq).getResultList();
            assertFalse("No Employees were returned", result.isEmpty());
            for (Employee e : result){
                assertFalse("PhoneNumbers Found", e.getPhoneNumbers().isEmpty());
            }
        }finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    public void testNullIf(){
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try{
            CriteriaBuilder qb = em.getCriteriaBuilder();
            CriteriaQuery<Employee> cq = qb.createQuery(Employee.class);
            Root<Employee> emp = cq.from(Employee.class);
            cq.where(qb.isNull(qb.nullif(qb.size(emp.<Collection<PhoneNumber>>get("phoneNumbers")), qb.parameter(Integer.class))));
            List<Employee> result = em.createQuery(cq).getResultList();
            assertFalse("No Employees were returned", result.isEmpty());
            for (Employee e : result){
                assertFalse("PhoneNumbers Found", e.getPhoneNumbers().isEmpty());
            }
        }finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    public void testNullRestrictionGetRestriction() {
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try {
            Metamodel mm = em.getMetamodel();
            CriteriaBuilder qbuilder = em.getCriteriaBuilder();
            CriteriaQuery<Employee> cquery = qbuilder.createQuery(Employee.class);
            cquery.getRestriction();
        }catch (NullPointerException ex){
            fail("'getRestriction()' with null restriction threw NullPointerException");
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    public void testIsMember(){

        EntityManager em = createEntityManager();
        beginTransaction(em);
        try{
            CriteriaBuilder qb = em.getCriteriaBuilder();
            CriteriaQuery<Employee> cq = qb.createQuery(Employee.class);
            Root<Employee> emp = cq.from(Employee.class);
            cq.where(qb.isMember(qb.parameter(String.class,"1"), emp.<Collection<String>>get("responsibilities")));
            TypedQuery<Employee> query = em.createQuery(cq);
            query.setParameter("1", "Sort files");
            List<Employee> result = query.getResultList();
            assertFalse("No Employees were returned", result.isEmpty());
            for (Employee e : result){
                assertTrue("Employee Found without Responcibilities", e.getResponsibilities().contains("Sort files"));

            }
        }finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }

    }


    public void testIsMemberEntity(){

        EntityManager em = createEntityManager();
        beginTransaction(em);
        try{
            CriteriaBuilder qb = em.getCriteriaBuilder();
            CriteriaQuery<Employee> cq = qb.createQuery(Employee.class);
            Root<Employee> emp = cq.from(Employee.class);
            Root<PhoneNumber> phone = cq.from(PhoneNumber.class);
            cq.where(qb.and(qb.equal(phone.get("areaCode"), "416"), qb.isMember(phone, emp.<Collection<PhoneNumber>>get("phoneNumbers"))));
            TypedQuery<Employee> query = em.createQuery(cq);
            List<Employee> result = query.getResultList();
            assertFalse("No Employees were returned", result.isEmpty());
            for (Employee e : result){
                boolean areacode = false;
                for (PhoneNumber p : e.getPhoneNumbers()){
                    areacode = areacode || p.getAreaCode().equals("416");
                }
                assertTrue("No PhoneNumbers with '416'area code", areacode);

            }
        }finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }

    }

    public void testVerySimpleJoin(){
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try{
            CriteriaQuery<Employee> cq = em.getCriteriaBuilder().createQuery(Employee.class);
            CriteriaBuilder qb = em.getCriteriaBuilder();
            Root<Employee> root = cq.from(em.getMetamodel().entity(Employee.class));
            root.join("phoneNumbers");
            if (usesSOP() && getPersistenceUnitServerSession().getPlatform().isOracle()) {
                // distinct is incompatible with blob in selection clause on Oracle
            } else {
                cq.distinct(true);
            }
            TypedQuery<Employee> tq = em.createQuery(cq);
            List<Employee> result = tq.getResultList();
            for (Employee emp : result){
                assertFalse("Found someone without a phone", emp.getPhoneNumbers().isEmpty());
            }
        }finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    public void testSimpleJoin(){
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try{
            CriteriaQuery<Employee> cq = em.getCriteriaBuilder().createQuery(Employee.class);
            CriteriaBuilder qb = em.getCriteriaBuilder();
            Root<Employee> root = cq.from(em.getMetamodel().entity(Employee.class));
            root.join("phoneNumbers");
            cq.where(qb.isEmpty(root.<Collection<PhoneNumber>>get("phoneNumbers")));
            TypedQuery<Employee> tq = em.createQuery(cq);
            List<Employee> result = tq.getResultList();
            assertTrue("Found employee but joins should have canceled isEmpty", result.isEmpty());
        }finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    public void testSimpleWhere(){
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try{
            CriteriaQuery<Employee> cq = em.getCriteriaBuilder().createQuery(Employee.class);
            CriteriaBuilder qb = em.getCriteriaBuilder();
            Root<Employee> root = cq.from(em.getMetamodel().entity(Employee.class));
            cq.where(qb.equal(root.get("firstName"), qb.literal("Bob")));
            TypedQuery<Employee> tq = em.createQuery(cq);
            List<Employee> result = tq.getResultList();
            assertFalse("No Employees were returned", result.isEmpty());
            assertEquals("Did not return Employee", result.get(0).getClass(), Employee.class);
            assertTrue("Employee had wrong firstname", result.get(0).getFirstName().equalsIgnoreCase("bob"));
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    public void testWhereDisjunction(){
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try{
            CriteriaQuery<Employee> cq = em.getCriteriaBuilder().createQuery(Employee.class);
            CriteriaBuilder qb = em.getCriteriaBuilder();
            cq.where(qb.disjunction());
            TypedQuery<Employee> tq = em.createQuery(cq);
            List<Employee> result = tq.getResultList();
            assertTrue("Employees were returned", result.isEmpty());
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    //Added for bug 413084
    public void testWhereNotDisjunction(){
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try{
            CriteriaQuery<Employee> cq = em.getCriteriaBuilder().createQuery(Employee.class);
            CriteriaBuilder qb = em.getCriteriaBuilder();
            cq.where(qb.disjunction().not());
            TypedQuery<Employee> tq = em.createQuery(cq);
            List<Employee> result = tq.getResultList();
            assertFalse("Employees were not returned", result.isEmpty());
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    public void testWhereConjunction(){
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try{
            CriteriaQuery<Employee> cq = em.getCriteriaBuilder().createQuery(Employee.class);
            CriteriaBuilder qb = em.getCriteriaBuilder();
            cq.where(qb.conjunction());
            TypedQuery<Employee> tq = em.createQuery(cq);
            List<Employee> result = tq.getResultList();
            assertFalse("Employees were not returned", result.isEmpty());
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    public void testWhereNotConjunction(){
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try{
            CriteriaQuery<Employee> cq = em.getCriteriaBuilder().createQuery(Employee.class);
            CriteriaBuilder qb = em.getCriteriaBuilder();
            cq.where(qb.conjunction().not());
            TypedQuery<Employee> tq = em.createQuery(cq);
            List<Employee> result = tq.getResultList();
            assertTrue("Employees were returned", result.isEmpty());
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    public void testJoinDistinct(){
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try{
            CriteriaBuilder qbuilder = em.getCriteriaBuilder();
        CriteriaQuery<Employee> cquery = qbuilder.createQuery(Employee.class);
        Root<Employee> customer = cquery.from(Employee.class);
        Fetch<Employee, Project> o = customer.fetch("phoneNumbers", JoinType.LEFT);
        cquery.where(customer.get("address").get("city").in("Ottawa", "Halifax"));
        cquery.select(customer).distinct(true);
        TypedQuery<Employee> tquery = em.createQuery(cquery);
        if (usesSOP() && getPersistenceUnitServerSession().getPlatform().isOracle()) {
            // distinct is incompatible with blob in selection clause on Oracle
            tquery.setHint(QueryHints.SERIALIZED_OBJECT, "false");
        }
        List<Employee> result = tquery.getResultList();
        assertFalse ("No results found", result.isEmpty());
        Long count = (Long)em.createQuery("Select count(e) from Employee e where e.address.city in ('Ottawa', 'Halifax')").getSingleResult();
            assertEquals("Incorrect number of results returned", result.size(), (long) count);
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }

    }

    public void testWhereConjunctionAndDisjunction(){
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try{
            CriteriaQuery<Employee> cq = em.getCriteriaBuilder().createQuery(Employee.class);
            CriteriaBuilder qb = em.getCriteriaBuilder();
            cq.where(qb.and(qb.disjunction(), qb.conjunction()));
            TypedQuery<Employee> tq = em.createQuery(cq);
            List<Employee> result = tq.getResultList();
            assertTrue("Employees were returned", result.isEmpty());
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    public void testWhereDisjunctionAndConjunction(){
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try{
            CriteriaQuery<Employee> cq = em.getCriteriaBuilder().createQuery(Employee.class);
            CriteriaBuilder qb = em.getCriteriaBuilder();
            cq.where(qb.and(qb.conjunction(), qb.disjunction()));
            TypedQuery<Employee> tq = em.createQuery(cq);
            List<Employee> result = tq.getResultList();
            assertTrue("Employees were returned", result.isEmpty());
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    public void testWhereConjunctionOrDisjunction(){
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try{
            CriteriaQuery<Employee> cq = em.getCriteriaBuilder().createQuery(Employee.class);
            CriteriaBuilder qb = em.getCriteriaBuilder();
            cq.where(qb.or(qb.disjunction(), qb.conjunction()));
            TypedQuery<Employee> tq = em.createQuery(cq);
            List<Employee> result = tq.getResultList();
            assertFalse("Employees were not returned", result.isEmpty());
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    public void testWhereUsingAndWithPredicates(){
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try{
            CriteriaQuery<Employee> cq = em.getCriteriaBuilder().createQuery(Employee.class);
            CriteriaBuilder qb = em.getCriteriaBuilder();
            Root<Employee> root = cq.from(em.getMetamodel().entity(Employee.class));
            cq.where(qb.and(qb.conjunction(), qb.equal(root.get("lastName"), "Smith")));
            TypedQuery<Employee> tq = em.createQuery(cq);
            List<Employee> result = tq.getResultList();
            assertFalse("Employees were not returned for 'true and lastName='Smith' '", result.isEmpty());

            cq = em.getCriteriaBuilder().createQuery(Employee.class);
            qb = em.getCriteriaBuilder();
            root = cq.from(em.getMetamodel().entity(Employee.class));
            cq.where(qb.and(qb.equal(root.get("lastName"), "Smith"), qb.conjunction()));
            tq = em.createQuery(cq);
            result = tq.getResultList();
            assertFalse("Employees were not returned for 'lastName='Smith' and true'", result.isEmpty());
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    public void testWhereUsingOrWithPredicates(){
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try{
            CriteriaQuery<Employee> cq = em.getCriteriaBuilder().createQuery(Employee.class);
            CriteriaBuilder qb = em.getCriteriaBuilder();
            Root<Employee> root = cq.from(em.getMetamodel().entity(Employee.class));
            cq.where(qb.or(qb.disjunction(), qb.equal(root.get("lastName"), "Smith")));
            TypedQuery<Employee> tq = em.createQuery(cq);
            List<Employee> result = tq.getResultList();
            assertFalse("Employees were not returned for 'false or lastName='Smith' '", result.isEmpty());

            cq = em.getCriteriaBuilder().createQuery(Employee.class);
            qb = em.getCriteriaBuilder();
            root = cq.from(em.getMetamodel().entity(Employee.class));
            cq.where(qb.or(qb.equal(root.get("lastName"), "Smith"), qb.disjunction()));
            tq = em.createQuery(cq);
            result = tq.getResultList();
            assertFalse("Employees were not returned for 'lastName='Smith' or false'", result.isEmpty());
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    public void testSimpleWhereObject(){
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try{
            CriteriaQuery<Employee> cq = em.getCriteriaBuilder().createQuery(Employee.class);
            CriteriaBuilder qb = em.getCriteriaBuilder();
            Root<Employee> root = cq.from(em.getMetamodel().entity(Employee.class));
            cq.where(qb.equal(root.get("firstName"), qb.literal("Bob")));
            TypedQuery<Employee> tq = em.createQuery(cq);
            List<Employee> result = tq.getResultList();
            assertFalse("No Employees were returned", result.isEmpty());
            assertEquals("Did not return Employee", result.get(0).getClass(), Employee.class);
            assertTrue("Employee had wrong firstname", result.get(0).getFirstName().equalsIgnoreCase("bob"));
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    public void testSimpleFetch(){
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try{
            CriteriaQuery<Employee> cq = em.getCriteriaBuilder().createQuery(Employee.class);
            CriteriaBuilder qb = em.getCriteriaBuilder();
            Root<Employee> root = cq.from(em.getMetamodel().entity(Employee.class));
            root.fetch("projects");
            cq.where(qb.equal(root.get("firstName"), qb.literal("Bob")));
            TypedQuery<Employee> tq = em.createQuery(cq);
            List<Employee> result = tq.getResultList();
            assertFalse("No Employees were returned", result.isEmpty());
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            try {
                ObjectOutputStream stream = new ObjectOutputStream(byteStream);

                stream.writeObject(result.get(0));
                stream.flush();
                byte[] arr = byteStream.toByteArray();
                ByteArrayInputStream inByteStream = new ByteArrayInputStream(arr);
                ObjectInputStream inObjStream = new ObjectInputStream(inByteStream);

                Employee emp = (Employee) inObjStream.readObject();
                assertEquals("Did not return Employee", emp.getClass(), Employee.class);
                assertTrue("Employee had wrong firstname", emp.getFirstName().equalsIgnoreCase("bob"));
                emp.getProjects().size(); //may cause exception
            } catch (IOException | ClassNotFoundException e) {
                fail("Failed during serialization");
            }
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }

    }


    public void testSize(){
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try{
  //          em.createQuery("Select size(e.responsibilities) from Employee e").getResultList();
            CriteriaBuilder qbuilder = em.getCriteriaBuilder();
            CriteriaQuery<Object[]> cquery = qbuilder.createQuery(Object[].class);
            Root<Employee> customer = cquery.from(Employee.class);
            cquery.select(qbuilder.array(customer.get("id"), qbuilder.size(customer.<Collection<String>>get("responsibilities"))));
            TypedQuery<Object[]> tquery = em.createQuery(cquery);
            List<Object[]> result = tquery.getResultList();
            for(Object[] value : result){
                assertEquals("Incorrect responsibilities count", em.find(Employee.class, value[0]).getResponsibilities().size(), (int) (Integer) value[1]);
            }
        // No assert as version is not actually a mapped field in dealer.
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    public void testSome(){
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try{
            em.createQuery("SELECT e from Employee e, IN(e.phoneNumbers) p where p.type = some(select p2.type from PhoneNumber p2 where p2.areaCode = '613')").getResultList();
            CriteriaBuilder qbuilder = em.getCriteriaBuilder();
        CriteriaQuery<Employee> cquery = qbuilder.createQuery(Employee.class);
           // Get Root Customer
           Root<Employee> customer = cquery.from(Employee.class);


           // Join Customer-Order
           Join<Employee, PhoneNumber> orders= customer.join("phoneNumbers");


          // create Subquery instance
          Subquery<String> sq = cquery.subquery(String.class);

          // Create Roots
          Root<PhoneNumber> order = sq.from(PhoneNumber.class);

           // Create SubQuery
           sq.select(order.get("type")).
      where(qbuilder.equal(order.get("areaCode"), "613"));

        // Create Main Query with SubQuery
      cquery.where(qbuilder.equal(orders.<String>get("type"), qbuilder.some(sq)));
      if (usesSOP() && getPersistenceUnitServerSession().getPlatform().isOracle()) {
          // distinct is incompatible with blob in selection clause on Oracle
      } else {
          cquery.distinct(true);
      }
      em.createQuery(cquery).getResultList();

        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    public void testSubQuery(){
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try{
            CriteriaBuilder qbuilder = em.getCriteriaBuilder();
            CriteriaQuery<Employee> cquery = qbuilder.createQuery(Employee.class);
            Root<Employee> customer = cquery.from(Employee.class);
            Join<Employee, Dealer> o = customer.join("dealers");
            if (usesSOP() && getPersistenceUnitServerSession().getPlatform().isOracle()) {
                // distinct is incompatible with blob in selection clause on Oracle
                cquery.select(customer);
            } else {
                cquery.select(customer).distinct(true);
            }
            Subquery<Integer> sq = cquery.subquery(Integer.class);
            Root<Dealer> sqo = sq.from(Dealer.class);
            sq.select(qbuilder.min(sqo.get("version")));
            cquery.where(qbuilder.equal(o.get("version"), sq));
            TypedQuery<Employee> tquery = em.createQuery(cquery);
            List<Employee> result = tquery.getResultList();
        // No assert as version is not actually a mapped field in dealer.
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    public void testFromToExpression() {
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try{
            JpaCriteriaBuilder cb = (JpaCriteriaBuilder)em.getCriteriaBuilder();
            CriteriaQuery<Employee> cquery = cb.createQuery(Employee.class);
            Root<Employee> emp = cquery.from(Employee.class);
            cquery.where(cb.fromExpression(cb.toExpression(emp).get("id").notNull()));
            TypedQuery<Employee> tquery = em.createQuery(cquery);
            List<Employee> result = tquery.getResultList();
            result.size();
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    protected static Set<Integer> getIds(Collection<Employee> employees) {
        Set<Integer> ids = new HashSet<>(employees.size());
        for (Employee emp : employees) {
            ids.add(emp.getId());
        }
        return ids;
    }
    protected void compareIds(List<Employee> jpqlEmployees, List<Employee> criteriaEmployees) {
        Set<Integer> jpqlIds = getIds(jpqlEmployees);
        Set<Integer> criteriaIds = getIds(criteriaEmployees);
        if (!jpqlIds.equals(criteriaIds)) {
            fail("jpql: " + jpqlIds + "; criteria: " + criteriaIds);
        }
    }

    public void testSubqueryExists() {
        EntityManager em = createEntityManager();
        List<Employee> jpqlEmployees;
        List<Employee> criteriaEmployees;
        beginTransaction(em);
        try {
            jpqlEmployees = em.createQuery("SELECT e FROM Employee e WHERE EXISTS (SELECT p FROM e.projects p)", Employee.class).getResultList();
            em.clear();
            CriteriaBuilder qbuilder = em.getCriteriaBuilder();
            CriteriaQuery<Employee> cquery = qbuilder.createQuery(Employee.class);
            Root<Employee> customer = cquery.from(Employee.class);
            // create correlated subquery
            Subquery<Project> sq = cquery.subquery(Project.class);
            Root<Employee> sqc = sq.correlate(customer);
            Path<Project> sqo = sqc.join("projects");
            sq.select(sqo);
            cquery.where(qbuilder.exists(sq));
            TypedQuery<Employee> tquery = em.createQuery(cquery);
            criteriaEmployees = tquery.getResultList();
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
        compareIds(jpqlEmployees, criteriaEmployees);
        for (Employee emp : criteriaEmployees){
            assertFalse("Found someone without projects", emp.getProjects().isEmpty());
        }
    }

    public void testSubqueryNotExists() {
        EntityManager em = createEntityManager();
        List<Employee> jpqlEmployees;
        List<Employee> criteriaEmployees;
        beginTransaction(em);
        try {
            jpqlEmployees = em.createQuery("SELECT e FROM Employee e WHERE NOT EXISTS (SELECT p FROM e.projects p)", Employee.class).getResultList();
            em.clear();
            CriteriaBuilder qbuilder = em.getCriteriaBuilder();
            CriteriaQuery<Employee> cquery = qbuilder.createQuery(Employee.class);
            Root<Employee> customer = cquery.from(Employee.class);
            // create correlated subquery
            Subquery<Project> sq = cquery.subquery(Project.class);
            Root<Employee> sqc = sq.correlate(customer);
            Path<Project> sqo = sqc.join("projects");
            sq.select(sqo);
            cquery.where(qbuilder.not(qbuilder.exists(sq)));
            TypedQuery<Employee> tquery = em.createQuery(cquery);
            criteriaEmployees = tquery.getResultList();
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
        compareIds(jpqlEmployees, criteriaEmployees);
        for (Employee emp : criteriaEmployees){
            assertTrue("Found someone with projects", emp.getProjects().isEmpty());
        }
    }

    // cquery.where(qbuilder.and(isMale, qbuilder.exists(sq)));
    public void testSubqueryExistsAfterAnd() {
        EntityManager em = createEntityManager();
        List<Employee> jpqlEmployees;
        List<Employee> criteriaEmployees;
        beginTransaction(em);
        try {
            jpqlEmployees = em.createQuery("SELECT e FROM Employee e WHERE e.gender = org.eclipse.persistence.testing.models.jpa.advanced.Employee.Gender.Male AND EXISTS (SELECT p FROM e.projects p)", Employee.class).getResultList();
            em.clear();
            CriteriaBuilder qbuilder = em.getCriteriaBuilder();
            CriteriaQuery<Employee> cquery = qbuilder.createQuery(Employee.class);
            Root<Employee> customer = cquery.from(Employee.class);
            // create correlated subquery
            Subquery<Project> sq = cquery.subquery(Project.class);
            Root<Employee> sqc = sq.correlate(customer);
            Path<Project> sqo = sqc.join("projects");
            sq.select(sqo);
            Predicate isMale = qbuilder.equal(customer.get("gender"), Employee.Gender.Male);
            cquery.where(qbuilder.and(isMale, qbuilder.exists(sq)));
            TypedQuery<Employee> tquery = em.createQuery(cquery);
            criteriaEmployees = tquery.getResultList();
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
        compareIds(jpqlEmployees, criteriaEmployees);
        for (Employee emp : criteriaEmployees){
            assertTrue("Found someone not male", emp.getGender() != null && emp.isMale());
            assertFalse("Found someone without projects", emp.getProjects().isEmpty());
        }
    }

    // cquery.where(qbuilder.and(qbuilder.exists(sq), isMale));
    public void testSubqueryExistsBeforeAnd() {
        EntityManager em = createEntityManager();
        List<Employee> jpqlEmployees;
        List<Employee> criteriaEmployees;
        beginTransaction(em);
        try {
            jpqlEmployees = em.createQuery("SELECT e FROM Employee e WHERE EXISTS (SELECT p FROM e.projects p) AND e.gender = org.eclipse.persistence.testing.models.jpa.advanced.Employee.Gender.Male", Employee.class).getResultList();
            em.clear();
            CriteriaBuilder qbuilder = em.getCriteriaBuilder();
            CriteriaQuery<Employee> cquery = qbuilder.createQuery(Employee.class);
            Root<Employee> customer = cquery.from(Employee.class);
            // create correlated subquery
            Subquery<Project> sq = cquery.subquery(Project.class);
            Root<Employee> sqc = sq.correlate(customer);
            Path<Project> sqo = sqc.join("projects");
            sq.select(sqo);
            Predicate isMale = qbuilder.equal(customer.get("gender"), Employee.Gender.Male);
            cquery.where(qbuilder.and(qbuilder.exists(sq), isMale));
            TypedQuery<Employee> tquery = em.createQuery(cquery);
            criteriaEmployees = tquery.getResultList();
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
        compareIds(jpqlEmployees, criteriaEmployees);
        for (Employee emp : criteriaEmployees){
            assertTrue("Found someone not male", emp.getGender() != null && emp.isMale());
            assertFalse("Found someone without projects", emp.getProjects().isEmpty());
        }
    }

    // cquery.where(qbuilder.and(isFemale, qbuilder.not(qbuilder.exists(sq))));
    public void testSubqueryNotExistsAfterAnd() {
        EntityManager em = createEntityManager();
        List<Employee> jpqlEmployees;
        List<Employee> criteriaEmployees;
        beginTransaction(em);
        try {
            jpqlEmployees = em.createQuery("SELECT e FROM Employee e WHERE e.gender = org.eclipse.persistence.testing.models.jpa.advanced.Employee.Gender.Female AND NOT EXISTS (SELECT p FROM e.projects p)", Employee.class).getResultList();
            em.clear();
            CriteriaBuilder qbuilder = em.getCriteriaBuilder();
            CriteriaQuery<Employee> cquery = qbuilder.createQuery(Employee.class);
            Root<Employee> customer = cquery.from(Employee.class);
            // create correlated subquery
            Subquery<Project> sq = cquery.subquery(Project.class);
            Root<Employee> sqc = sq.correlate(customer);
            Path<Project> sqo = sqc.join("projects");
            sq.select(sqo);
            Predicate isFemale = qbuilder.equal(customer.get("gender"), Employee.Gender.Female);
            cquery.where(qbuilder.and(isFemale, qbuilder.not(qbuilder.exists(sq))));
            TypedQuery<Employee> tquery = em.createQuery(cquery);
            criteriaEmployees = tquery.getResultList();
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
        compareIds(jpqlEmployees, criteriaEmployees);
        for (Employee emp : criteriaEmployees){
            assertTrue("Found someone not female", emp.getGender() != null && emp.isFemale());
            assertTrue("Found someone with projects", emp.getProjects().isEmpty());
        }
    }

    // cquery.where(qbuilder.and(qbuilder.not(qbuilder.exists(sq)), isFemale));
    public void testSubqueryNotExistsBeforeAnd() {
        EntityManager em = createEntityManager();
        List<Employee> jpqlEmployees;
        List<Employee> criteriaEmployees;
        beginTransaction(em);
        try {
            jpqlEmployees = em.createQuery("SELECT e FROM Employee e WHERE NOT EXISTS (SELECT p FROM e.projects p) AND e.gender = org.eclipse.persistence.testing.models.jpa.advanced.Employee.Gender.Female", Employee.class).getResultList();
            em.clear();
            CriteriaBuilder qbuilder = em.getCriteriaBuilder();
            CriteriaQuery<Employee> cquery = qbuilder.createQuery(Employee.class);
            Root<Employee> customer = cquery.from(Employee.class);
            // create correlated subquery
            Subquery<Project> sq = cquery.subquery(Project.class);
            Root<Employee> sqc = sq.correlate(customer);
            Path<Project> sqo = sqc.join("projects");
            sq.select(sqo);
            Predicate isFemale = qbuilder.equal(customer.get("gender"), Employee.Gender.Female);
            cquery.where(qbuilder.and(qbuilder.not(qbuilder.exists(sq)), isFemale));
            TypedQuery<Employee> tquery = em.createQuery(cquery);
            criteriaEmployees = tquery.getResultList();
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
        compareIds(jpqlEmployees, criteriaEmployees);
        for (Employee emp : criteriaEmployees){
            assertTrue("Found someone not female", emp.getGender() != null && emp.isFemale());
            assertTrue("Found someone with projects", emp.getProjects().isEmpty());
        }
    }

    public void testSubqueryExistsNested() {
        EntityManager em = createEntityManager();
        List<Employee> jpqlEmployees;
        List<Employee> criteriaEmployees;
        beginTransaction(em);
        try {
            jpqlEmployees = em.createQuery("SELECT e FROM Employee e join e.projects ep WHERE EXISTS (SELECT p FROM Project p WHERE ep = p AND EXISTS (SELECT t FROM Employee t WHERE p.teamLeader = t))", Employee.class).getResultList();
            em.clear();

            CriteriaBuilder builder = em.getCriteriaBuilder();
            CriteriaQuery<Employee> mainQuery = builder.createQuery(Employee.class);
            Subquery<Object> subQuery1 = mainQuery.subquery(Object.class);
            Subquery<Object> subQuery2 = subQuery1.subquery(Object.class);

            Root<Employee> mainEmployee = mainQuery.from(Employee.class);
            mainQuery.select(mainEmployee);

            Root<Project> sub1Project = subQuery1.from(Project.class);
            Join<Employee, Project> mainEmployeeProjects = mainEmployee.join("projects");

            Root<Employee> sub2Employee = subQuery2.from(Employee.class);
            Join<Employee, Employee> sub1ProjectTeamLeader = sub1Project.join("teamLeader");

            subQuery2.where(builder.equal(sub2Employee, sub1ProjectTeamLeader));
            subQuery1.where(builder.and(builder.exists(subQuery2), builder.equal(sub1Project, mainEmployeeProjects)));
            mainQuery.where(builder.exists(subQuery1));

            TypedQuery<Employee> tquery = em.createQuery(mainQuery);
            criteriaEmployees = tquery.getResultList();
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
        compareIds(jpqlEmployees, criteriaEmployees);
        for (Employee emp : criteriaEmployees){
            assertFalse("Found someone without projects", emp.getProjects().isEmpty());
            boolean atLeastOneProjectHasLeader = false;
            for (Project proj : emp.getProjects()) {
                if (proj.getTeamLeader() != null) {
                    atLeastOneProjectHasLeader = true;
                    break;
                }
            }
            assertTrue("None of employee's projects has a leader", atLeastOneProjectHasLeader);
        }
    }

    public void testSubqueryExistsNestedUnusedRoot() {
        EntityManager em = createEntityManager();
        List<Employee> jpqlEmployees;
        List<Employee> criteriaEmployees;
        beginTransaction(em);
        try {
            jpqlEmployees = em.createQuery("SELECT e FROM Employee e join e.projects ep WHERE EXISTS (SELECT p FROM Project p WHERE ep = p AND EXISTS (SELECT t FROM Employee t WHERE p.teamLeader = t))", Employee.class).getResultList();
            em.clear();

            CriteriaBuilder builder = em.getCriteriaBuilder();
            CriteriaQuery<Employee> mainQuery = builder.createQuery(Employee.class);
            Subquery<Object> subQuery1 = mainQuery.subquery(Object.class);
            Subquery<Object> subQuery2 = subQuery1.subquery(Object.class);

            // Add an unused Root
            mainQuery.from(Dealer.class);

            Root<Employee> mainEmployee = mainQuery.from(Employee.class);

            // Add another unused Root
            mainQuery.from(Address.class);

            mainQuery.select(mainEmployee);

            Root<Project> sub1Project = subQuery1.from(Project.class);
            Join<Employee, Project> mainEmployeeProjects = mainEmployee.join("projects");

            Root<Employee> sub2Employee = subQuery2.from(Employee.class);
            Join<Employee, Employee> sub1ProjectTeamLeader = sub1Project.join("teamLeader");

            subQuery2.where(builder.equal(sub2Employee, sub1ProjectTeamLeader));
            subQuery1.where(builder.and(builder.exists(subQuery2), builder.equal(sub1Project, mainEmployeeProjects)));
            mainQuery.where(builder.exists(subQuery1));

            TypedQuery<Employee> tquery = em.createQuery(mainQuery);
            criteriaEmployees = tquery.getResultList();
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
        compareIds(jpqlEmployees, criteriaEmployees);
        for (Employee emp : criteriaEmployees){
            assertFalse("Found someone without projects", emp.getProjects().isEmpty());
            boolean atLeastOneProjectHasLeader = false;
            for (Project proj : emp.getProjects()) {
                if (proj.getTeamLeader() != null) {
                    atLeastOneProjectHasLeader = true;
                    break;
                }
            }
            assertTrue("None of employee's projects has a leader", atLeastOneProjectHasLeader);
        }
    }

    public void testSubqueryExistsNestedAfterAnd() {
        EntityManager em = createEntityManager();
        List<Employee> jpqlEmployees;
        List<Employee> criteriaEmployees;
        beginTransaction(em);
        try {
            jpqlEmployees = em.createQuery("SELECT e FROM Employee e join e.projects ep WHERE e.gender = org.eclipse.persistence.testing.models.jpa.advanced.Employee.Gender.Male AND EXISTS (SELECT p FROM Project p WHERE 'Sales Reporting' <> p.name AND ep = p AND EXISTS (SELECT t FROM Employee t WHERE p.teamLeader = t))", Employee.class).getResultList();
            em.clear();

            CriteriaBuilder builder = em.getCriteriaBuilder();
            CriteriaQuery<Employee> mainQuery = builder.createQuery(Employee.class);
            Subquery<Object> subQuery1 = mainQuery.subquery(Object.class);
            Subquery<Object> subQuery2 = subQuery1.subquery(Object.class);

            Root<Employee> mainEmployee = mainQuery.from(Employee.class);
            mainQuery.select(mainEmployee);

            Root<Project> sub1Project = subQuery1.from(Project.class);
            Join<Employee, Project> mainEmployeeProjects = mainEmployee.join("projects");

            Root<Employee> sub2Employee = subQuery2.from(Employee.class);
            Join<Employee, Employee> sub1ProjectTeamLeader = sub1Project.join("teamLeader");

            subQuery2.where(builder.equal(sub2Employee, sub1ProjectTeamLeader));
            Predicate notSalesReporting = builder.not(builder.equal(builder.literal("Sales Reporting"), sub1Project.get("name")));
            subQuery1.where(builder.and(notSalesReporting, builder.and(builder.exists(subQuery2), builder.equal(sub1Project, mainEmployeeProjects))));
            Predicate isMale = builder.equal(mainEmployee.get("gender"), Employee.Gender.Male);
            mainQuery.where(builder.and(isMale, builder.exists(subQuery1)));

            TypedQuery<Employee> tquery = em.createQuery(mainQuery);
            criteriaEmployees = tquery.getResultList();
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
        compareIds(jpqlEmployees, criteriaEmployees);
        for (Employee emp : criteriaEmployees){
            assertTrue("Found someone not male", emp.getGender() != null && emp.isMale());
            assertFalse("Found someone without projects", emp.getProjects().isEmpty());
            boolean atLeastOneProjectHasLeader = false;
            for (Project proj : emp.getProjects()) {
                if (!proj.getName().equals("Sales Reporting")) {
                    if (proj.getTeamLeader() != null) {
                        atLeastOneProjectHasLeader = true;
                        break;
                    }
                }
            }
            assertTrue("None of employee's projects has a leader", atLeastOneProjectHasLeader);
        }
    }

    public void testSubqueryExistsNestedAfterLiteralAnd() {
        EntityManager em = createEntityManager();
        List<Employee> jpqlEmployees;
        List<Employee> criteriaEmployees;
        beginTransaction(em);
        try {
            jpqlEmployees = em.createQuery("SELECT e FROM Employee e join e.projects ep WHERE EXISTS (SELECT p FROM Project p WHERE ep = p AND EXISTS (SELECT t FROM Employee t WHERE p.teamLeader = t))", Employee.class).getResultList();
            em.clear();

            CriteriaBuilder builder = em.getCriteriaBuilder();
            CriteriaQuery<Employee> mainQuery = builder.createQuery(Employee.class);
            Subquery<Object> subQuery1 = mainQuery.subquery(Object.class);
            Subquery<Object> subQuery2 = subQuery1.subquery(Object.class);

            Root<Employee> mainEmployee = mainQuery.from(Employee.class);
            mainQuery.select(mainEmployee);

            Root<Project> sub1Project = subQuery1.from(Project.class);
            Join<Employee, Project> mainEmployeeProjects = mainEmployee.join("projects");

            Root<Employee> sub2Employee = subQuery2.from(Employee.class);
            Join<Employee, Employee> sub1ProjectTeamLeader = sub1Project.join("teamLeader");

            subQuery2.where(builder.equal(sub2Employee, sub1ProjectTeamLeader));
            Predicate oneEqualsOne = builder.equal(builder.literal(1), builder.literal(1));
            subQuery1.where(builder.and(oneEqualsOne, builder.and(builder.exists(subQuery2), builder.equal(sub1Project, mainEmployeeProjects))));
            Predicate twoEqualsTwo = builder.equal(builder.literal(2), builder.literal(2));
            mainQuery.where(builder.and(twoEqualsTwo, builder.exists(subQuery1)));

            TypedQuery<Employee> tquery = em.createQuery(mainQuery);
            criteriaEmployees = tquery.getResultList();
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
        compareIds(jpqlEmployees, criteriaEmployees);
        for (Employee emp : criteriaEmployees){
            assertFalse("Found someone without projects", emp.getProjects().isEmpty());
            boolean atLeastOneProjectHasLeader = false;
            for (Project proj : emp.getProjects()) {
                if (proj.getTeamLeader() != null) {
                    atLeastOneProjectHasLeader = true;
                    break;
                }
            }
            assertTrue("None of employee's projects has a leader", atLeastOneProjectHasLeader);
        }
    }

    /**
     * Test cursored queries.
     */
    public void testCursors() {
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try {
            // Test cursored stream.
            Query query = em.createQuery(em.getCriteriaBuilder().createQuery(Employee.class));
            query.setHint(QueryHints.CURSOR, true);
            query.setHint(QueryHints.CURSOR_INITIAL_SIZE, 2);
            query.setHint(QueryHints.CURSOR_PAGE_SIZE, 5);
            query.setHint(QueryHints.CURSOR_SIZE, "Select count(*) from CMP3_EMPLOYEE");
            Cursor cursor = (Cursor)query.getSingleResult();
            cursor.nextElement();
            cursor.size();
            cursor.close();

            // Test cursor result API.
            JpaQuery<Employee> jpaQuery = (JpaQuery<Employee>)((EntityManager)em.getDelegate()).createQuery(em.getCriteriaBuilder().createQuery(Employee.class));
            jpaQuery.setHint(QueryHints.CURSOR, true);
            cursor = jpaQuery.getResultCursor();
            cursor.nextElement();
            cursor.size();
            cursor.close();

            // Test scrollable cursor.
            jpaQuery = (JpaQuery<Employee>)((EntityManager)em.getDelegate()).createQuery(em.getCriteriaBuilder().createQuery(Employee.class));
            jpaQuery.setHint(QueryHints.SCROLLABLE_CURSOR, true);
            jpaQuery.setHint(QueryHints.RESULT_SET_CONCURRENCY, ResultSetConcurrency.ReadOnly);
            String resultSetType = ResultSetType.DEFAULT;
            // HANA supports only TYPE_FORWARD_ONLY, see bug 384116
            if (getPlatform().isHANA()) {
                resultSetType = ResultSetType.ForwardOnly;
            }
            jpaQuery.setHint(QueryHints.RESULT_SET_TYPE, resultSetType);
            ScrollableCursor scrollableCursor = (ScrollableCursor)jpaQuery.getResultCursor();
            scrollableCursor.next();
            scrollableCursor.close();

        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    /**
     * Test the result type of various queries.
     */
    @SuppressWarnings({"unchecked"})
    public void testObjectResultType() {
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try {
            // Load an employee into the cache.
            TypedQuery<Employee> query = em.createQuery(em.getCriteriaBuilder().createQuery(Employee.class));
            List<Employee> result = query.getResultList();
            Employee employee = result.get(0);

            CriteriaBuilder qb = em.getCriteriaBuilder();
            // Test multi object, as an array.
            CriteriaQuery<Object[]> cq = qb.createQuery(Object[].class);
            Root<Employee> from = cq.from(Employee.class);
            cq.multiselect(from, from.get("address"), from.get("id"));
            Parameter<String> firstNameParam = qb.parameter(from.<String>get("firstName").getModel().getBindableJavaType(), "firstName");
            cq.where(qb.and(qb.equal(from.get("id"), qb.parameter(from.get("id").getModel().getBindableJavaType(), "id")),qb.equal(from.get("firstName"), firstNameParam)));
            TypedQuery<Object[]> oQuery = em.createQuery(cq);
            oQuery.setParameter("id", employee.getId());
            oQuery.setParameter(firstNameParam, employee.getFirstName());
            Object[] arrayResult = oQuery.getSingleResult();
            if ((arrayResult.length != 3) && (arrayResult[0] != employee) || (arrayResult[1] != employee.getAddress()) || (!arrayResult[2].equals(employee.getId()))) {
                fail("Array result not correct: " + Arrays.toString(arrayResult));
            }
            List<Object[]> listResult = oQuery.getResultList();
            arrayResult = listResult.get(0);
            if ((arrayResult.length != 3) || (arrayResult[0] != employee) || (arrayResult[1] != employee.getAddress()) || (!arrayResult[2].equals(employee.getId()))) {
                fail("Array result not correct: " + Arrays.toString(arrayResult));
            }

            // Test single object, as an array.
            cq = qb.createQuery(Object[].class);
            from = cq.from(Employee.class);
            cq.multiselect(from.get("id"));
            cq.where(qb.and(qb.equal(from.get("id"), qb.parameter(from.get("id").getModel().getBindableJavaType(), "id")), (qb.equal(from.get("firstName"), qb.parameter(from.get("firstName").getModel().getBindableJavaType(), "firstName")))));
            oQuery = em.createQuery(cq);
            oQuery.setHint(QueryHints.RESULT_TYPE, ResultType.Array);
            oQuery.setParameter("id", employee.getId());
            oQuery.setParameter("firstName", employee.getFirstName());
            arrayResult = oQuery.getSingleResult();
            if ((arrayResult.length != 1) || (!arrayResult[0].equals(employee.getId()))) {
                fail("Array result not correct: " + Arrays.toString(arrayResult));
            }
            listResult = oQuery.getResultList();
            arrayResult = listResult.get(0);
            if ((arrayResult.length != 1) || (!arrayResult[0].equals(employee.getId()))) {
                fail("Array result not correct: " + Arrays.toString(arrayResult));
            }

            // Test multi object, as a Map.
            cq = qb.createQuery(Object[].class);
            from = cq.from(Employee.class);
            cq.multiselect(from.alias("employee"), from.get("address").alias("address"), from.get("id").alias("id"));
            cq.where(qb.and(qb.equal(from.get("id"), qb.parameter(from.get("id").getModel().getBindableJavaType(), "id")), qb.equal(from.get("firstName"), qb.parameter(from.get("firstName").getModel().getBindableJavaType(), "firstName"))));
            TypedQuery<?> mapQuery = em.createQuery(cq);
            mapQuery.setHint(QueryHints.RESULT_TYPE, ResultType.Map);
            mapQuery.setParameter("id", employee.getId());
            mapQuery.setParameter("firstName", employee.getFirstName());
            Map<?, ?> mapResult = (Map<?, ?>)mapQuery.getSingleResult();
            if ((mapResult.size() != 3) ||(mapResult.get("employee") != employee) || (mapResult.get("address") != employee.getAddress()) || (!mapResult.get("id").equals(employee.getId()))) {
                fail("Map result not correct: " + mapResult);
            }
            List<Map<?, ?>> mapListResult = (List<Map<?, ?>>) mapQuery.getResultList();
            mapResult = mapListResult.get(0);
            if ((mapResult.size() != 3) ||(mapResult.get("employee") != employee) || (mapResult.get("address") != employee.getAddress()) || (!mapResult.get("id").equals(employee.getId()))) {
                fail("Map result not correct: " + mapResult);
            }

            // Test single object, as a Map.
            cq = qb.createQuery(Object[].class);
            from = cq.from(Employee.class);
            cq.multiselect(from.get("id").alias("id"));
            cq.where(qb.and(qb.equal(from.get("id"), qb.parameter(from.get("id").getModel().getBindableJavaType(), "id")), qb.equal(from.get("firstName"), qb.parameter(from.get("firstName").getModel().getBindableJavaType(), "firstName"))));
            mapQuery = em.createQuery(cq);
            mapQuery.setHint(QueryHints.RESULT_TYPE, ResultType.Map);
            mapQuery.setParameter("id", employee.getId());
            mapQuery.setParameter("firstName", employee.getFirstName());
            mapResult = (Map<?, ?>) mapQuery.getSingleResult();
            if ((mapResult.size() != 1) || (!mapResult.get("id").equals(employee.getId()))) {
                fail("Map result not correct: " + mapResult);
            }
            mapListResult = (List<Map<?, ?>>) mapQuery.getResultList();
            mapResult = mapListResult.get(0);
            if ((mapResult.size() != 1) || (!mapResult.get("id").equals(employee.getId()))) {
                fail("Map result not correct: " + mapResult);
            }

            // Test single object, as an array.
            CriteriaQuery<Employee> empCq = qb.createQuery(Employee.class);
            from = empCq.from(Employee.class);
            empCq.where(qb.and(qb.equal(from.get("id"), qb.parameter(from.get("id").getModel().getBindableJavaType(), "id")), qb.equal(from.get("firstName"), qb.parameter(from.get("firstName").getModel().getBindableJavaType(), "firstName"))));
            TypedQuery<?> arrQuery = em.createQuery(empCq);
            arrQuery.setHint(QueryHints.QUERY_TYPE, QueryType.Report);
            arrQuery.setHint(QueryHints.RESULT_TYPE, ResultType.Array);
            arrQuery.setParameter("id", employee.getId());
            arrQuery.setParameter("firstName", employee.getFirstName());
            arrayResult = (Object[])arrQuery.getSingleResult();
            if (arrayResult[0] != employee) {
                fail("Array result not correct: " + Arrays.toString(arrayResult));
            }

            // Test single object, as value.
            cq = qb.createQuery(Object[].class);
            from = cq.from(Employee.class);
            cq.multiselect(from.get("id"));
            cq.where(qb.and(qb.equal(from.get("id"), qb.parameter(from.get("id").getModel().getBindableJavaType(), "id")), qb.equal(from.get("firstName"), qb.parameter(from.get("firstName").getModel().getBindableJavaType(), "firstName"))));
            oQuery = em.createQuery(cq);
            oQuery.setParameter("id", employee.getId());
            oQuery.setParameter("firstName", employee.getFirstName());
            Object valueResult = oQuery.getSingleResult();
            if (! valueResult.equals(employee.getId())) {
                fail("Value result not correct: " + valueResult);
            }
            listResult = oQuery.getResultList();
            valueResult = listResult.get(0);
            if (! valueResult.equals(employee.getId())) {
                fail("Value result not correct: " + valueResult);
            }

            // Test multi object, as value.
            cq = qb.createQuery(Object[].class);
            from = cq.from(Employee.class);
            cq.multiselect(from.get("id"), from.get("firstName"));
            cq.where(qb.and(qb.equal(from.get("id"), qb.parameter(from.get("id").getModel().getBindableJavaType(), "id")), qb.equal(from.get("firstName"), qb.parameter(from.get("firstName").getModel().getBindableJavaType(), "firstName"))));
            oQuery = em.createQuery(cq);
            oQuery.setHint(QueryHints.RESULT_TYPE, ResultType.Value);
            oQuery.setParameter("id", employee.getId());
            oQuery.setParameter("firstName", employee.getFirstName());
            valueResult = oQuery.getSingleResult();
            if (! valueResult.equals(employee.getId())) {
                fail("Value result not correct: " + valueResult);
            }

            // Test single object, as attribute.
            cq = qb.createQuery(Object[].class);
            from = cq.from(Employee.class);
            cq.multiselect(from.get("id"));
            cq.where(qb.and(qb.equal(from.get("id"), qb.parameter(from.get("id").getModel().getBindableJavaType(), "id")), qb.equal(from.get("firstName"), qb.parameter(from.get("firstName").getModel().getBindableJavaType(), "firstName"))));
            oQuery = em.createQuery(cq);
            oQuery.setHint(QueryHints.RESULT_TYPE, ResultType.Attribute);
            oQuery.setParameter("id", employee.getId());
            oQuery.setParameter("firstName", employee.getFirstName());
            valueResult = oQuery.getSingleResult();
            if (! valueResult.equals(employee.getId())) {
                fail("Value result not correct: " + valueResult);
            }
            listResult = oQuery.getResultList();
            valueResult = listResult.get(0);
            if (! valueResult.equals(employee.getId())) {
                fail("Value result not correct: " + valueResult);
            }
        } finally {
            rollbackTransaction(em);
        }
    }

    /**
     * Test that a cache hit will occur on a primary key query.
     */
    public void testQueryExactPrimaryKeyCacheHits() {
        EntityManager em = createEntityManager();
        beginTransaction(em);
        QuerySQLTracker counter = null;
        try {
            // Load an employee into the cache.
            CriteriaBuilder qb = em.getCriteriaBuilder();
            CriteriaQuery<Employee> cq = qb.createQuery(Employee.class);
            TypedQuery<Employee> query = em.createQuery(cq);
            List<Employee> result = query.getResultList();
            Employee employee = result.get(0);

            // Count SQL.
            counter = new QuerySQLTracker(getPersistenceUnitServerSession());
            // Query by primary key.
            cq = qb.createQuery(Employee.class);
            Root<Employee> from = cq.from(Employee.class);
            cq.where(qb.equal(from.get("id"), qb.parameter(from.get("id").getModel().getBindableJavaType(), "id")));
            query = em.createQuery(cq);
            query.setHint(QueryHints.CACHE_USAGE, CacheUsage.CheckCacheByExactPrimaryKey);
            query.setParameter("id", employee.getId());
            Employee queryResult = query.getSingleResult();
            if (queryResult != employee) {
                fail("Employees are not equal: " + employee + ", " + queryResult);
            }
            if (counter.getSqlStatements().size() > 0) {
                fail("Cache hit do not occur: " + counter.getSqlStatements());
            }
        } finally {
            rollbackTransaction(em);
            if (counter != null) {
                counter.remove();
            }
        }
    }

    public void testQueryHintFetch(){
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try{
            CriteriaQuery<Employee> cq = em.getCriteriaBuilder().createQuery(Employee.class);
            CriteriaBuilder qb = em.getCriteriaBuilder();
            Root<Employee> root = cq.from(em.getMetamodel().entity(Employee.class));
            cq.where(qb.equal(root.get("firstName"), qb.literal("Bob")));
            TypedQuery<Employee> tq = em.createQuery(cq);
            tq.setHint(QueryHints.FETCH, "e.projects");
            List<Employee> result = tq.getResultList();
            assertFalse("No Employees were returned", result.isEmpty());
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            try {
                ObjectOutputStream stream = new ObjectOutputStream(byteStream);

                stream.writeObject(result.get(0));
                stream.flush();
                byte[] arr = byteStream.toByteArray();
                ByteArrayInputStream inByteStream = new ByteArrayInputStream(arr);
                ObjectInputStream inObjStream = new ObjectInputStream(inByteStream);

                Employee emp = (Employee) inObjStream.readObject();
                assertEquals("Did not return Employee", emp.getClass(), Employee.class);
                assertTrue("Employee had wrong firstname", emp.getFirstName().equalsIgnoreCase("bob"));
                emp.getProjects().size(); //may cause exception
            } catch (IOException | ClassNotFoundException e) {
                fail("Failed during serialization");
            }
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }

    }

    public void testProd(){
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try{
  //          em.createQuery("Select size(e.responsibilities) from Employee e").getResultList();
            CriteriaBuilder qbuilder = em.getCriteriaBuilder();
            CriteriaQuery<BigInteger> cquery = qbuilder.createQuery(BigInteger.class);
            Root<Employee> customer = cquery.from(Employee.class);
            cquery.select(qbuilder.toBigInteger(qbuilder.prod(qbuilder.literal(BigInteger.valueOf(5)),customer.<Integer>get("salary"))));
            TypedQuery<BigInteger> tquery = em.createQuery(cquery);
            List<BigInteger> result = tquery.getResultList();
            for(BigInteger value : result){
                assertEquals("Incorrect arithmatic returned ", value.mod(BigInteger.valueOf(5)), BigInteger.valueOf(0));
            }
        // No assert as version is not actually a mapped field in dealer.
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    /**
     * Test that a cache hit will occur on a query.
     */
    public void testQueryCacheFirstCacheHits() {
        EntityManager em = createEntityManager();
        beginTransaction(em);
        QuerySQLTracker counter = null;
        try {
            // Load an employee into the cache.
            CriteriaBuilder qb = em.getCriteriaBuilder();
            CriteriaQuery<Employee> cq = qb.createQuery(Employee.class);
            TypedQuery<Employee> query = em.createQuery(cq);
            List<Employee> result = query.getResultList();
            Employee employee = result.get(result.size() - 1);

            // Count SQL.
            counter = new QuerySQLTracker(getPersistenceUnitServerSession());
            // Query by primary key.
            cq = qb.createQuery(Employee.class);
            Root<Employee> from = cq.from(Employee.class);
            cq.where(qb.equal(from.get("firstName"), qb.parameter(from.get("firstName").getModel().getBindableJavaType(), "firstName")));
            query = em.createQuery(cq);
            query.setHint(QueryHints.CACHE_USAGE, CacheUsage.CheckCacheThenDatabase);
            query.setParameter("firstName", employee.getFirstName());
            Employee queryResult = query.getSingleResult();
            if (!queryResult.getFirstName().equals(employee.getFirstName())) {
                fail("Employees are not equal: " + employee + ", " + queryResult);
            }
            if (counter.getSqlStatements().size() > 0) {
                fail("Cache hit do not occur: " + counter.getSqlStatements());
            }
        } finally {
            rollbackTransaction(em);
            if (counter != null) {
                counter.remove();
            }
        }
    }

    /**
     * Test that a cache hit will occur on a query.
     */
    public void testQueryCacheOnlyCacheHits() {
        EntityManager em = createEntityManager();
        beginTransaction(em);
        QuerySQLTracker counter = null;
        try {
            // Load an employee into the cache.
            CriteriaBuilder qb = em.getCriteriaBuilder();
            CriteriaQuery<Employee> cq = qb.createQuery(Employee.class);
            TypedQuery<Employee> query = em.createQuery(cq);
            List<Employee> result = query.getResultList();
            Employee employee = result.get(result.size() - 1);

            // Count SQL.
            counter = new QuerySQLTracker(getPersistenceUnitServerSession());
            // Query by primary key.
            cq = qb.createQuery(Employee.class);
            Root<Employee> from = cq.from(Employee.class);
            cq.where(qb.equal(from.get("firstName"), qb.parameter(from.get("firstName").getModel().getBindableJavaType(), "firstName")));
            query = em.createQuery(cq);
            query.setHint(QueryHints.CACHE_USAGE, CacheUsage.CheckCacheOnly);
            query.setParameter("firstName", employee.getFirstName());
            // Test that list works as well.
            query.getResultList();
            if (counter.getSqlStatements().size() > 0) {
                fail("Cache hit do not occur: " + counter.getSqlStatements());
            }
        } finally {
            rollbackTransaction(em);
            if (counter != null) {
                counter.remove();
            }
        }
    }

    /**
     * Test that a cache hit will occur on a query when the object is not in the unit of work/em.
     */
    public void testQueryCacheOnlyCacheHitsOnSession() {
        EntityManager em = createEntityManager();
        beginTransaction(em);
        QuerySQLTracker counter = null;
        try {
            // Load an employee into the cache.
            CriteriaBuilder qb = em.getCriteriaBuilder();
            CriteriaQuery<Employee> cq = qb.createQuery(Employee.class);
            TypedQuery<Employee> query = em.createQuery(cq);
            List<Employee> result = query.getResultList();
            Employee employee = result.get(result.size() - 1);

            // Count SQL.
            counter = new QuerySQLTracker(getPersistenceUnitServerSession());
            // Query by primary key.
            rollbackTransaction(em);
            closeEntityManager(em);
            em = createEntityManager();
            beginTransaction(em);
            cq = qb.createQuery(Employee.class);
             Root<Employee> from = cq.from(Employee.class);
            cq.where(qb.equal(from.get("id"), qb.parameter(from.get("id").getModel().getBindableJavaType(), "id")));
            query = em.createQuery(cq);
            query.setHint(QueryHints.QUERY_TYPE, QueryType.ReadObject);
            query.setHint(QueryHints.CACHE_USAGE, CacheUsage.CheckCacheOnly);
            query.setParameter("id", employee.getId());
            if (query.getSingleResult() == null) {
                fail("Query did not check session cache.");
            }
            if (counter.getSqlStatements().size() > 0) {
                fail("Cache hit do not occur: " + counter.getSqlStatements());
            }
            rollbackTransaction(em);
            closeEntityManager(em);
            em = createEntityManager();
            beginTransaction(em);
            cq = qb.createQuery(Employee.class);
            from = cq.from(Employee.class);
            cq.where(qb.equal(from.get("id"), qb.parameter(from.get("id").getModel().getBindableJavaType(), "id")));
            query = em.createQuery(cq);
            query.setHint(QueryHints.CACHE_USAGE, CacheUsage.CheckCacheOnly);
            query.setParameter("id", employee.getId());
            if (query.getResultList().size() != 1) {
                fail("Query did not check session cache.");
            }
            if (counter.getSqlStatements().size() > 0) {
                fail("Cache hit do not occur: " + counter.getSqlStatements());
            }
        } finally {
            if (counter != null) {
                counter.remove();
            }
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    /**
     * bug 413892: tests that unused inner join expressions from root.get("manager") do not affect explicit out joins
     * created from root.join("manager").
     */
    public void testUnusedJoinDoesNotAffectOtherJoins() {
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try{
            CriteriaBuilder qbuilder = em.getCriteriaBuilder();
            CriteriaQuery<Employee> cquery = qbuilder.createQuery(Employee.class);
            Root<Employee> customer = cquery.from(Employee.class);
            Path<Object> pathToIgnore = customer.get("manager").get("address");
            Join<Object, Object> manager = customer.join("manager", JoinType.LEFT);

            TypedQuery<Employee> tquery = em.createQuery(cquery);
            List<Employee> result = tquery.getResultList();
            assertFalse ("No results found", result.isEmpty());
            long count = (Long)em.createQuery("Select count(e) from Employee e ").getSingleResult();
            assertEquals("Incorrect number of results returned", result.size(), count);
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    /**
     * bug 413892: tests that unused inner join expressions from root.get("manager") do not affect explicit outer joins
     * created from root.fetch("manager").
     */
    public void testUnusedJoinDoesNotAffectFetchJoin() {
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try{
            CriteriaBuilder qbuilder = em.getCriteriaBuilder();
            CriteriaQuery<Employee> cquery = qbuilder.createQuery(Employee.class);
            Root<Employee> customer = cquery.from(Employee.class);
            Path<Object> pathToIgnore = customer.get("manager").get("address");
            customer.fetch("manager", JoinType.LEFT);

            TypedQuery<Employee> tquery = em.createQuery(cquery);
            List<Employee> result = tquery.getResultList();
            assertFalse ("No results found", result.isEmpty());
            long count = (Long)em.createQuery("Select count(e) from Employee e ").getSingleResult();
            assertEquals("Incorrect number of results returned", result.size(), count);
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }
    
    /**
     * Bug 464833 - Criteria API: calling getRestriction() on a query returns Predicate with incorrect expression
     * An incorrect expression is observed when calling getRestriction() on an existing query to obtain an existing Predicate. 
     * Tests: Validate that an existing Predicate (obtained with criteriaQuery.getRestriction()) has correct child expressions.  
     */
    public void testGetRestrictionReturningCorrectPredicate() {
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try {
            CriteriaBuilder builder = em.getCriteriaBuilder();
            CriteriaQuery<Employee> criteriaQuery = builder.createQuery(Employee.class);
            Root<Employee> root = criteriaQuery.from(Employee.class);
            
            // simple case - construct a predicate
            criteriaQuery.where(builder.equal(root.get("firstName"), "Bob"));
            TypedQuery<Employee> query1 = em.createQuery(criteriaQuery);
            
            List<Employee> results1 = query1.getResultList();
            long count1 = (Long)em.createQuery("select count(e) from Employee e where e.firstName = 'Bob'").getSingleResult();
            
            // validate the expressions on the Predicate returned from CriteriaQuery getRestriction()
            Predicate predicate = criteriaQuery.getRestriction();
            
            // for the current example, the Predicate returned is expected to be a CompoundExpressionImpl
            assertNotNull("Predicate should be non-null", predicate);
            assertTrue("Invalid predicate type returned: " + predicate.getClass().getName(), predicate instanceof CompoundExpressionImpl);
            CompoundExpressionImpl compoundExpression = (CompoundExpressionImpl)predicate;
            
            // The where has two child expressions representing:
            // 1) a path (query key) for "firstName" and 2) an expression (constant) for "Bob".
            List<Expression<?>> expressions = compoundExpression.getChildExpressions();
            assertSame("Predicate should have two child expressions", 2, expressions.size());
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }

    /**
     * Test that checks duplicating of joins
     */
    @SuppressWarnings({"unchecked"})
    public void testJoinDuplication() throws NoSuchFieldException, IllegalAccessException {
        EntityManager em = createEntityManager();
        beginTransaction(em);
        try {
            CriteriaBuilder qb = em.getCriteriaBuilder();
            CriteriaQuery<Employee>cq = qb.createQuery(Employee.class);
            Root<Employee> root = cq.from(Employee.class);
            root.join("manager");

            em.createQuery(cq);
            Field field = cq.getClass().getDeclaredField("joins");
            field.setAccessible(true);
            Set<FromImpl> value = (Set<FromImpl>) field.get(cq);
            assertEquals(1, value.size());

            em.createQuery(cq);
            ((CriteriaQueryImpl<Employee>)cq).translate();
            value = (Set<FromImpl>) field.get(cq);
            assertEquals(1, value.size());
        } finally {
            rollbackTransaction(em);
            closeEntityManager(em);
        }
    }


}
